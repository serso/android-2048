package org.solovyev.android.game2048;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontUtils;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;

import javax.annotation.Nonnull;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.andengine.engine.options.ScreenOrientation.PORTRAIT_FIXED;
import static org.andengine.util.HorizontalAlign.CENTER;
import static org.solovyev.android.game2048.Board.newBoard;
import static org.solovyev.android.game2048.CellStyle.newCellStyle;

public class GameActivity extends SimpleBaseGameActivity {

	@Nonnull
	private final SparseArray<CellStyle> cellStyles = new SparseArray<CellStyle>();

	@Nonnull
	private final CellStyle lastCellStyle = newCellStyle(2048, R.color.cell_text_2048, R.color.cell_bg_2048);

	{
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
	private final Board board = newBoard().random();

	@Nonnull
	private final Fonts fonts = new Fonts(this);

	@Nonnull
	private GestureDetector gestureDetector;

	@Override
	protected void onCreateResources() {
		for (int i = 0; i < cellStyles.size(); i++) {
			cellStyles.valueAt(i).loadFont(this, d.cellTextSize);
		}
	}

	@Nonnull
	public Fonts getFonts() {
		return fonts;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gestureDetector = new GestureDetector(this, new GestureListener());
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		gestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setBackground(new Background(getColor(R.color.bg)));
		scene.attachChild(createBoard());

		return scene;
	}

	@Nonnull
	private IEntity createBoard() {
		final Entity board = new Entity(d.board.left, d.board.top);
		final Rectangle boardRect = new Rectangle(0, 0, d.board.width(), d.board.height(), getVertexBufferObjectManager());
		boardRect.setColor(getColor(R.color.board_bg));
		board.attachChild(boardRect);

		for (int i = 0; i < Board.SIZE; i++) {
			for (int j = 0; j < Board.SIZE; j++) {
				board.attachChild(createCell(i, j));
			}
		}

		return board;
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
	private IEntity createCell(int i, int j) {
		final Board.Cell c = board.getCell(i, j);
		float x = i * d.cellSize + (i + 1) * d.cellPadding;
		float y = j * d.cellSize + (j + 1) * d.cellPadding;
		final Rectangle cell = new Rectangle(x, y, d.cellSize, d.cellSize, getVertexBufferObjectManager());

		final CellStyle cellStyle = cellStyles.get(c.getValue(), lastCellStyle);
		cell.setColor(getColor(cellStyle.getBgColorResId()));
		if (c.hasValue()) {
			final String cellValue = String.valueOf(c.getValue());
			Font cellFont = cellStyle.getFont();
			float textWidth = FontUtils.measureText(cellFont, cellValue);
			float textHeight = cellFont.getLineHeight();
			if (textWidth > d.cellTextSize) {
				cellFont = cellStyle.getFont(this, d.cellTextSize * d.cellTextSize / textWidth);
				textWidth = FontUtils.measureText(cellFont, cellValue);
				textHeight = cellFont.getLineHeight();
			}
			cell.attachChild(new Text(d.cellSize / 2 - textWidth / 2, d.cellSize / 2 - textHeight * 5 / 12, cellFont, cellValue, new TextOptions(CENTER), getVertexBufferObjectManager()));
		}
		return cell;
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		d.calculate(this);
		final Camera camera = new Camera(0, 0, d.width, d.height);
		return new EngineOptions(true, PORTRAIT_FIXED, new RatioResolutionPolicy(d.width, d.height), camera);
	}

	private static final class Dimensions {

		private final Rect board = new Rect();
		private float cellPadding;
		private float cellSize;
		private float cellTextSize;
		private float width;
		private float height;

		private Dimensions() {
		}

		private void calculate(@Nonnull Activity activity) {
			final Point displaySize = getDisplaySize(activity);
			width = min(displaySize.x, displaySize.y);
			height = max(displaySize.x, displaySize.y);
			calculateBoard();
			cellPadding = board.width() / 30f;
			cellSize = (board.width() - (Board.SIZE + 1) * cellPadding) / Board.SIZE;
			cellTextSize = cellSize * 2 / 3;
		}

		private float calculateBoard() {
			final float boardSize = min(width, height) * 5f / 6f;
			board.left = (int) (width / 2 - boardSize / 2);
			board.top = (int) (height / 2 - boardSize / 2);
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
			final float xDiff = e2.getX() - e1.getX();
			final float yDiff = e2.getY() - e1.getY();
			if (Math.abs(xDiff) > swipeMaxOffPath && Math.abs(yDiff) > swipeMaxOffPath) {
				return false;
			}

			final boolean xVelocityOk = Math.abs(xVelocity) > swipeThresholdVelocity;
			final boolean yVelocityOk = Math.abs(yVelocity) > swipeThresholdVelocity;
			if (Math.abs(xDiff) > Math.abs(yDiff)) {
				if(!checkLeftRightSwipes(xDiff, xVelocityOk)) {
					checkUpDownSwipes(yDiff, yVelocityOk);
				}
			} else {
				if(!checkUpDownSwipes(yDiff, yVelocityOk)) {
					checkLeftRightSwipes(xDiff, xVelocityOk);
				}
			}

			return false;
		}

		private boolean checkLeftRightSwipes(float xDiff, boolean xVelocityOk) {
			if (xDiff > swipeMinDistance && xVelocityOk) {
				App.showToast("Right swipe");
				return true;
			} else if (-xDiff > swipeMinDistance && xVelocityOk) {
				App.showToast("Left swipe");
				return true;
			}

			return false;
		}

		private boolean checkUpDownSwipes(float yDiff, boolean yVelocityOk) {
			if (-yDiff > swipeMinDistance && yVelocityOk) {
				App.showToast("Up swipe");
				return true;
			} else if (yDiff > swipeMinDistance && yVelocityOk) {
				App.showToast("Down swipe");
				return true;
			}

			return false;
		}
	}
}
