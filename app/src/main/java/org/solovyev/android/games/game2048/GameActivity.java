package org.solovyev.android.games.game2048;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.*;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontUtils;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseSineInOut;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.AndroidMenuHelper;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.andengine.engine.options.ScreenOrientation.PORTRAIT_FIXED;
import static org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory.createFromAsset;
import static org.andengine.util.HorizontalAlign.CENTER;
import static org.solovyev.android.Activities.restartActivity;
import static org.solovyev.android.games.game2048.CellStyle.newCellStyle;

public class GameActivity extends SimpleBaseGameActivity {

	@Nonnull
	private final SparseArray<CellStyle> cellStyles = new SparseArray<CellStyle>();

	@Nonnull
	private final CellStyle lastCellStyle = newCellStyle(2048, R.color.cell_text_2048, R.color.cell_bg_2048);

	{
		cellStyles.append(-1, newCellStyle(0, R.color.cell_wall_text, R.color.cell_wall_bg));
		cellStyles.append(0, newCellStyle(0, R.color.cell_text, R.color.cell_bg));
		cellStyles.append(2, newCellStyle(2, R.color.cell_text_2, R.color.cell_bg_2));
		cellStyles.append(4, newCellStyle(4, R.color.cell_text_4, R.color.cell_bg_4));
		cellStyles.append(8, newCellStyle(8, R.color.cell_text_8, R.color.cell_bg_8));
		cellStyles.append(16, newCellStyle(16, R.color.cell_text_16, R.color.cell_bg_16));
		cellStyles.append(32, newCellStyle(32, R.color.cell_text_32, R.color.cell_bg_32));
		cellStyles.append(64, newCellStyle(64, R.color.cell_text_64, R.color.cell_bg_64));
		cellStyles.append(128, newCellStyle(128, R.color.cell_text_128, R.color.cell_bg_128));
		cellStyles.append(256, newCellStyle(256, R.color.cell_text_256, R.color.cell_bg_256));
		cellStyles.append(512, newCellStyle(512, R.color.cell_text_512, R.color.cell_bg_512));
		cellStyles.append(1024, newCellStyle(1024, R.color.cell_text_1024, R.color.cell_bg_1024));
		cellStyles.append(2048, lastCellStyle);
	}

	@Nonnull
	private final Dimensions d = new Dimensions();

	@Nonnull
	private final Fonts fonts = new Fonts(this);

	@Nonnull
	private TextureRegion preferencesButtonTexture;

	@Nonnull
	private TextureRegion shareButtonTexture;

	@Nonnull
	private TextureRegion restartButtonTexture;

	@Nonnull
	private GestureDetector gestureDetector;

	private volatile boolean animating = false;
	private volatile boolean initializing = true;

	@Nullable
	private ActivityMenu<Menu, MenuItem> menu;

	private Scene scene;
	private Text scoreText;

	@Nonnull
	private Game game;

	@Nonnull
	private final Object gameLock = new Object();

	@Nonnull
	private final HighScores highScores = App.getHighScores();

	private IEntity gameOverView;

	@Override
	protected void onCreateResources() {
		for (int i = 0; i < cellStyles.size(); i++) {
			cellStyles.valueAt(i).loadFont(this, d.cellTextSize);
		}
		preferencesButtonTexture = loadTexture("preferences-icon.png");
		shareButtonTexture = loadTexture("share-icon.png");
		restartButtonTexture = loadTexture("restart-icon.png");
	}

	@Nonnull
	private TextureRegion loadTexture(@Nonnull String name) {
		final BitmapTextureAtlas bitmapTextureAtlas = new BitmapTextureAtlas(getTextureManager(), 128, 128, TextureOptions.BILINEAR);
		final TextureRegion texture = createFromAsset(bitmapTextureAtlas, this, name, 0, 0);
		bitmapTextureAtlas.load();
		return texture;
	}

	@Nonnull
	public Fonts getFonts() {
		return fonts;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initializing = true;
		game = App.getGame();
		synchronized (gameLock) {
			// as game is shared between activities we need to cleanup all views attached to the objects
			game.releaseViews();
			scene = null;
			scoreText = null;
			gameOverView = null;
		}

		super.onCreate(savedInstanceState);
		gestureDetector = new GestureDetector(this, new GestureListener());
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!animating && !initializing && isGameRunning() && gameOverView == null) {
			gestureDetector.onTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		scene = new Scene();
		scene.setBackground(new Background(getColor(R.color.bg)));

		final String appName = getString(R.string.app_name);
		final Font titleFont = getFonts().getFont(d.titleSize, R.color.text, appName.length(), true);
		final Text titleText = new Text(d.title.x, d.title.y, titleFont, appName, getVertexBufferObjectManager());
		scene.attachChild(titleText);

		final Sprite preferencesButton = createButton(d.button.x, d.button.y, preferencesButtonTexture, new Runnable() {
			@Override
			public void run() {
				showPreferences();
			}
		});
		scene.attachChild(preferencesButton);
		scene.registerTouchArea(preferencesButton);

		final Font scoreFont = getFonts().getFont(d.textSize, R.color.text, 20);
		scoreText = new Text(d.score.x, d.score.y, scoreFont, getString(R.string.score_with_highscore), 40, getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
					showHighScores();
				}

				return true;
			}
		};
		updateScore();
		scene.attachChild(scoreText);
		scene.registerTouchArea(scoreText);

		scene.attachChild(createBoard());

		final String rules = getString(R.string.rules);
		final Font rulesFont = getFonts().getFont(d.rulesSize, R.color.text, 10);
		scene.attachChild(new Text(d.rules.x, d.rules.y, rulesFont, rules, 1024, new TextOptions(AutoWrap.WORDS, d.board.width()), getVertexBufferObjectManager()));

		if (game.isOver()) {
			onGameOver();
		}

		initializing = false;

		return scene;
	}

	@Nonnull
	private Sprite createButton(final float x, final float y, @Nonnull final TextureRegion texture, @Nonnull final Runnable runnable) {
		final Sprite button = new Sprite(x, y, texture, this.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
					runnable.run();
				}

				return true;
			}
		};
		button.setSize(d.buttonSize, d.buttonSize);
		return button;
	}

	@Nonnull
	private IEntity createBoard() {
		synchronized (gameLock) {
			final Entity boardView = new Entity(d.board.left, d.board.top);
			final Rectangle boardRect = new Rectangle(0, 0, d.board.width(), d.board.height(), getVertexBufferObjectManager());
			boardRect.setColor(getColor(R.color.board_bg));
			boardView.attachChild(boardRect);

			final Board board = game.getBoard();
			board.setView(boardView);
			final int size = board.getSize();
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					boardView.attachChild(createNotValueCell(i, j));
				}
			}

			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					final IEntity cell = createValueCell(i, j);
					if (cell != null) {
						boardView.attachChild(cell);
					}
				}
			}

			return boardView;
		}
	}

	@Nullable
	private IEntity createValueCell(int i, int j) {
		final Cell c = game.getBoard().getCell(i, j);
		if (c.hasValue()) {
			return createValueCell(i, j, c);
		} else {
			return null;
		}
	}

	@Nonnull
	private IEntity createValueCell(int i, int j, @Nonnull Cell cell) {
		final Rectangle cellView = createCell(i, j);
		final String cellValue = String.valueOf(cell.getValue());
		final CellStyle cellStyle = cellStyles.get(cell.getValue(), lastCellStyle);
		cellView.setColor(getColor(cellStyle.getBgColorResId()));
		Font cellFont = cellStyle.getFont();
		float textWidth = FontUtils.measureText(cellFont, cellValue);
		float textHeight = cellFont.getLineHeight();
		if (textWidth > d.cellTextSize) {
			cellFont = cellStyle.getFont(this, d.cellTextSize * d.cellTextSize / textWidth);
			textWidth = FontUtils.measureText(cellFont, cellValue);
			textHeight = cellFont.getLineHeight();
		}
		cellView.attachChild(new Text(d.cellSize / 2 - textWidth / 2, d.cellSize / 2 - textHeight * 5 / 12, cellFont, cellValue, new TextOptions(CENTER), getVertexBufferObjectManager()));
		cell.setView(cellView);
		cellView.registerEntityModifier(new ScaleModifier(0.2f, 1f, 1.1f, new IEntityModifier.IEntityModifierListener() {
			@Override
			public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
			}

			@Override
			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
				cellView.registerEntityModifier(new ScaleModifier(0.2f, 1.1f, 1f));
			}
		}));
		return cellView;
	}

	@Nonnull
	Color getColor(int colorResId) {
		final int color = getResources().getColor(colorResId);
		return new Color(android.graphics.Color.red(color) / 255.f,
				android.graphics.Color.green(color) / 255.f,
				android.graphics.Color.blue(color) / 255.f,
				android.graphics.Color.alpha(color) / 255.f);
	}

	@Nonnull
	private IEntity createNotValueCell(int i, int j) {
		final Cell c = game.getBoard().getCell(i, j);
		if (c.isWall()) {
			final Rectangle cell = createCell(i, j);
			final CellStyle cellStyle = cellStyles.get(c.getValue(), lastCellStyle);
			cell.setColor(getColor(cellStyle.getBgColorResId()));
			return cell;
		} else {
			return createCell(i, j);
		}
	}

	private Rectangle createCell(int i, int j) {
		final Point position = newCellPosition(i, j);
		final Rectangle cell = new Rectangle(position.x, position.y, d.cellSize, d.cellSize, getVertexBufferObjectManager());

		final CellStyle cellStyle = cellStyles.get(0, lastCellStyle);
		cell.setColor(getColor(cellStyle.getBgColorResId()));
		return cell;
	}

	@Nonnull
	private Point newCellPosition(int row, int col) {
		final float x = col * d.cellSize + (col + 1) * d.cellPadding;
		final float y = row * d.cellSize + (row + 1) * d.cellPadding;
		return new Point((int) x, (int) y);
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		d.calculate(this, game);
		final Camera camera = new Camera(0, 0, d.width, d.height);
		return new EngineOptions(true, PORTRAIT_FIXED, new RatioResolutionPolicy(d.width, d.height), camera);
	}

	@Override
	protected void onPause() {
		game.save(App.getPreferences());
		super.onPause();
	}

	@Override
	protected synchronized void onResume() {
		super.onResume();

		if (!game.hasDefaultPreferences()) {
			restartGame();
		} else if (game.isOver()) {
			onGameOver();
		}
	}

	private static final class Dimensions {

		private float titleSize;
		private float buttonSize;
		private float buttonPadding;
		private float textSize;
		private float rulesSize;
		private Point title = new Point();
		private Point button = new Point();
		private Point score = new Point();
		private Point rules = new Point();
		private float padding;
		private float textPadding;
		private final Rect board = new Rect();
		private float cellPadding;
		private float cellSize;
		private float cellTextSize;
		private float width;
		private float height;

		private Dimensions() {
		}

		private void calculate(@Nonnull Activity activity, @Nonnull Game game) {
			final Point displaySize = getDisplaySize(activity);
			width = min(displaySize.x, displaySize.y);
			height = max(displaySize.x, displaySize.y);
			titleSize = 0.08f * height;
			buttonPadding = titleSize / 10f;
			buttonSize = titleSize - 2 * buttonPadding;
			textSize = titleSize / 2;
			rulesSize = textSize * 3 / 4;
			padding = titleSize;
			textPadding = textSize;
			title.y = (int) textPadding;
			button.y = (int) (title.y + 2 * buttonPadding);
			score.y = (int) (title.y + textPadding + titleSize);
			calculateBoard();
			final int size = game.getBoard().getSize();
			cellPadding = 0.15f * board.width() / size;
			cellSize = (board.width() - (size + 1) * cellPadding) / size;
			cellTextSize = cellSize * 2 / 3;
			title.x = board.left;
			button.x = (int) (board.right - buttonSize);
			score.x = board.left;
			rules.x = board.left;
			rules.y = (int) (board.bottom + textPadding);
		}

		private float calculateBoard() {
			final float desiredSize = min(width, height) * 5f / 6f;
			final float boardSize = min(desiredSize, height / 2);
			board.left = (int) (width / 2 - boardSize / 2);
			board.top = (int) (score.y + textSize + textPadding);
			board.right = (int) (board.left + boardSize);
			board.bottom = (int) (board.top + boardSize);
			return boardSize;
		}

		@Nonnull
		private static Point getDisplaySize(@Nonnull Activity activity) {
			final Display display = activity.getWindowManager().getDefaultDisplay();
			final Point displaySize = new Point();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				display.getSize(displaySize);
			} else {
				final DisplayMetrics dm = new DisplayMetrics();
				display.getMetrics(dm);

				displaySize.x = dm.widthPixels;
				displaySize.y = dm.heightPixels;
			}
			return displaySize;
		}
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		private final ViewConfiguration vc = ViewConfiguration.get(GameActivity.this);
		private final int swipeMinDistance = vc.getScaledPagingTouchSlop();
		private final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
		private final int swipeMaxOffPath = Integer.MAX_VALUE;

		public boolean onFling(MotionEvent e1, MotionEvent e2, float xVelocity, float yVelocity) {
			if (e1 == null || e2 == null) {
				return false;
			}

			final float xDiff = e2.getX() - e1.getX();
			final float yDiff = e2.getY() - e1.getY();
			if (Math.abs(xDiff) > swipeMaxOffPath && Math.abs(yDiff) > swipeMaxOffPath) {
				return false;
			}

			final boolean xVelocityOk = Math.abs(xVelocity) > swipeThresholdVelocity;
			final boolean yVelocityOk = Math.abs(yVelocity) > swipeThresholdVelocity;

			Direction direction;
			if (Math.abs(xDiff) > Math.abs(yDiff)) {
				direction = checkLeftRightSwipes(xDiff, xVelocityOk);
				if (direction == null) {
					checkUpDownSwipes(yDiff, yVelocityOk);
				}
			} else {
				direction = checkUpDownSwipes(yDiff, yVelocityOk);
				if (direction == null) {
					checkLeftRightSwipes(xDiff, xVelocityOk);
				}
			}

			if (direction != null) {
				go(direction);
			}

			return false;
		}

		@Nullable
		private Direction checkLeftRightSwipes(float xDiff, boolean xVelocityOk) {
			if (xDiff > swipeMinDistance && xVelocityOk) {
				return Direction.right;
			} else if (-xDiff > swipeMinDistance && xVelocityOk) {
				return Direction.left;
			}

			return null;
		}

		@Nullable
		private Direction checkUpDownSwipes(float yDiff, boolean yVelocityOk) {
			if (-yDiff > swipeMinDistance && yVelocityOk) {
				return Direction.up;
			} else if (yDiff > swipeMinDistance && yVelocityOk) {
				return Direction.down;
			}

			return null;
		}
	}

	private void go(@Nonnull Direction direction) {
		if (animating) {
			return;
		}

		final CellsAnimationListener cellsAnimationListener = new CellsAnimationListener();

		final List<CellChange.Move> moves = game.go(direction);
		for (CellChange.Move move : moves) {
			final Point from = newCellPosition(move.from.x, move.from.y);
			final Point to = newCellPosition(move.to.x, move.to.y);
			final IEntity cellView = move.cell.getView();
			cellView.registerEntityModifier(new MoveModifier(0.2f, from.x, to.x, from.y, to.y, cellsAnimationListener, EaseSineInOut.getInstance()));
			if (move instanceof CellChange.Move.Merge) {
				final CellChange.Move.Merge merge = (CellChange.Move.Merge) move;
				cellsAnimationListener.merges.add(merge);
			}
		}
	}

	private class CellsAnimationListener implements IEntityModifier.IEntityModifierListener {

		@Nonnull
		private final List<CellChange.Move.Merge> merges = new ArrayList<CellChange.Move.Merge>();

		private int count = 0;

		@Override
		public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
			count++;
			animating = true;
		}

		@Override
		public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
			count--;
			if (count == 0) {
				runOnUpdateThread(new Runnable() {
					@Override
					public void run() {
						synchronized (gameLock) {
							final IEntity boardView = game.getBoard().getView();

							updateScore();

							for (CellChange.Move.Merge merge : merges) {
								boardView.detachChild(merge.cell.getView());
								boardView.attachChild(createValueCell(merge.to.x, merge.to.y));
								boardView.detachChild(merge.removedCell.getView());
							}

							final List<CellChange.New> newCells = game.prepareNextTurn();
							for (CellChange.New newCell : newCells) {
								boardView.attachChild(createValueCell(newCell.position.x, newCell.position.y, newCell.cell));
							}

							if (game.isOver()) {
								onGameOver();
							}

							animating = false;
						}
					}
				});
			}
		}
	}

	private void onGameOver() {
		if (scene == null) {
			return;
		}
		if (gameOverView != null) {
			return;
		}

		gameOverView = createGameOverView();
		scene.attachChild(gameOverView);
	}

	@Nonnull
	private IEntity createGameOverView() {
		final Entity gameOverView = new Entity(d.board.left, d.board.top);
		final Rectangle gameOverRect = new Rectangle(0, 0, d.board.width(), d.board.height(), getVertexBufferObjectManager());
		gameOverRect.setColor(getColor(R.color.board_bg));
		gameOverRect.setAlpha(0.9f);
		gameOverView.attachChild(gameOverRect);

		final Text gameOverLabel = createGameOverLabel();
		gameOverView.attachChild(gameOverLabel);

		final Sprite gameOverRestartButton = createGameOverRestartButton(gameOverLabel);
		gameOverView.attachChild(gameOverRestartButton);
		scene.registerTouchArea(gameOverRestartButton);

		final Sprite gameOverShareButton = createGameOverShareButton(gameOverLabel);
		gameOverView.attachChild(gameOverShareButton);
		scene.registerTouchArea(gameOverShareButton);

		return gameOverView;
	}

	@Nonnull
	private Sprite createGameOverShareButton(@Nonnull Text gameOverLabel) {
		final float x = 3 * d.board.width() / 4 - d.buttonSize / 2;
		final float y = gameOverLabel.getY() + gameOverLabel.getHeight() + d.textPadding;
		return createButton(x, y, shareButtonTexture, new Runnable() {
			@Override
			public void run() {
				shareScore();
			}
		});
	}

	@Nonnull
	private Sprite createGameOverRestartButton(@Nonnull Text gameOverLabel) {
		final float x = d.board.width() / 4 - d.buttonSize / 2;
		final float y = gameOverLabel.getY() + gameOverLabel.getHeight() + d.textPadding;
		return createButton(x, y, restartButtonTexture, new Runnable() {
			@Override
			public void run() {
				restartGame();
			}
		});
	}

	@Nonnull
	private Text createGameOverLabel() {
		final String gameOver = getString(R.string.game_over);
		final Font gameOverFont = getFonts().getFont(d.titleSize, R.color.text_inverted, gameOver.length(), true);
		final float textWidth = FontUtils.measureText(gameOverFont, gameOver);
		final float textHeight = gameOverFont.getLineHeight();

		final float x = d.board.width() / 2 - textWidth / 2;
		final float y = d.board.height() / 2 - textHeight - d.textPadding / 2;
		return new Text(x, y, gameOverFont, gameOver, getVertexBufferObjectManager());
	}

	private void updateScore() {
		final HighScore highestScore = highScores.getHighestScore();
		final int scorePoints = game.getScore().getPoints();
		if (highestScore.hasPoints()) {
			final int highScorePoints = highestScore.getPoints();
			scoreText.setText(getString(R.string.score_with_highscore, scorePoints, highScorePoints));
		} else {
			scoreText.setText(getString(R.string.score, scorePoints));
		}
	}

	private void restartGame() {
		synchronized (gameLock) {
			if (highScores.addHighScore(game)) {
				highScores.save(App.getPreferences());
			}
			game.reset();
		}
		restartActivity(this);
	}

	private void shareScore() {
		final Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, game.getScore().getPoints(), App.SHARE_URL));
		shareIntent.setType("text/plain");
		startActivity(shareIntent);
	}

	private void showHighScores() {
		startActivity(new Intent(this, HighScoresActivity.class));
	}

	private void showPreferences() {
		startActivity(new Intent(this, PreferencesActivity.class));
	}

	/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return this.menu.onPrepareOptionsMenu(this, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if (this.menu == null) {
			final List<IdentifiableMenuItem<MenuItem>> items = new ArrayList<IdentifiableMenuItem<MenuItem>>();
			items.add(new RestartMenuItem());
			items.add(new HighScoresMenuItem());
			items.add(new PreferencesMenuItem());
			items.add(new ShareMenuItem());
			this.menu = ListActivityMenu.fromResource(R.menu.menu, items, AndroidMenuHelper.getInstance());
		}
		return this.menu.onCreateOptionsMenu(this, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this, item);
	}

	private final class RestartMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.menu_restart;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			restartGame();
		}
	}

	private final class HighScoresMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.menu_high_scores;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			showHighScores();
		}
	}

	private final class PreferencesMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.menu_preferences;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			showPreferences();
		}
	}

	private final class ShareMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.menu_share;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			shareScore();
		}
	}
}
