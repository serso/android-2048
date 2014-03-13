package org.solovyev.android.games.game2048;

public enum Difficulty {
	easy {
		@Override
		int getNewCellCount(int size) {
			return 1;
		}
	},

	normal {
		@Override
		int getNewCellCount(int size) {
			return 2;
		}
	},

	hard {
		@Override
		int getNewCellCount(int size) {
			return Math.max(2, size / 2 + size % 2);
		}
	};

	abstract int getNewCellCount(int size);
}
