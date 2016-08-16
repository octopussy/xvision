package com.borschlabs.xcom.gen;

import com.badlogic.gdx.math.Vector2;
import com.borschlabs.xcom.geometry.Poly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author octopussy
 */
public class DungeonChamber {
	public Field field;

	private int mapSize = 64;
	private int density = 40;

	public void generate() {
		//mapSize = Integer.parseInt(mapSizeTextField.getText());
		//density = Integer.parseInt(densityTextField.getText());

		Random r = new Random(new Random().nextLong());

		int iteration = 0;

		Field prevStep = new Field(mapSize);
		for (Cell c : prevStep) {
			c.wall = r.nextFloat() * 100 <= density;
		}


		while (iteration < 7) {
			Field thisStep = new Field(mapSize);
			for (Cell c : thisStep) {
				java.util.List<Cell> neighboursWithin1 = prevStep.pickCellsAround(c, 1);
				java.util.List<Cell> neighboursWithin2 = prevStep.pickCellsAround(c, 2);
				int wallsWithin1 = 9 - neighboursWithin1.size();
				for (Cell c1 : neighboursWithin1) {
					wallsWithin1 += c1.wall ? 1 : 0;
				}

				int wallsWithin2 = 25 - neighboursWithin2.size();
				for (Cell c1 : neighboursWithin2) {
					wallsWithin2 += c1.wall ? 1 : 0;
				}

				if (iteration < 4) {
					c.wall = wallsWithin1 >= 5 || wallsWithin2 <= 2;
				} else if (iteration < 7) {
					c.wall = wallsWithin1 >= 5;
				}
			}

			prevStep = thisStep;
			++iteration;
		}

		field = prevStep;
	}

	public static class Field implements Iterable<Cell> {
		public final int mapSize;
		private java.util.List<Cell> data = new ArrayList<Cell>();

		public Field(int mapSize) {
			this.mapSize = mapSize;
			for (int y = 0; y < mapSize; ++y) {
				for (int x = 0; x < mapSize; ++x) {
					data.add(new Cell(x, y, false));
				}
			}
		}

		@Override
		public Iterator<Cell> iterator() {
			return data.iterator();
		}

		public List<Cell> pickCellsAround(Cell c, int depth) {
			List<Cell> r = new ArrayList<Cell>();

			for (int y = c.y - depth; y <= c.y + depth; ++y) {
				for (int x = c.x - depth; x <= c.x + depth; ++x) {
					Cell c1 = getCell(x, y);
					if (c1 != null) {
						r.add(c1);
					}
				}
			}
			return r;
		}

		public Cell getCell(int x, int y) {
			if (x < 0 || x >= mapSize || y < 0 || y >= mapSize) {
				return null;
			}
			return data.get(y * mapSize + x);
		}

		public boolean isIsolatedWall(Cell c) {
			return c.x > 0 && c.x < mapSize - 1 && c.y > 0 && c.y < mapSize - 1 &&
				getCell(c.x + 1, c.y).wall &&
				getCell(c.x, c.y + 1).wall &&
				getCell(c.x - 1, c.y).wall &&
				getCell(c.x, c.y - 1).wall;
		}

		public List<Poly.Wall> getCellGeometry(Cell c, float side) {
			boolean hasLeft = c.x == 0 || getCell(c.x - 1, c.y).wall;
			boolean hasBottom = c.y == 0 || getCell(c.x, c.y - 1).wall;
			boolean hasRight = c.x == mapSize - 1 || getCell(c.x + 1, c.y).wall;
			boolean hasTop = c.y == mapSize - 1 || getCell(c.x, c.y + 1).wall;
			List<Poly.Wall> w = new ArrayList<Poly.Wall>();

			Poly.Wall bottom = new Poly.Wall(new Vector2((c.x + 1) * side, c.y * side),
				new Vector2(c.x * side, c.y * side));

			Poly.Wall right = new Poly.Wall(new Vector2((c.x + 1) * side, (c.y + 1) * side),
				new Vector2((c.x + 1) * side, c.y * side));

			Poly.Wall top = new Poly.Wall(
				new Vector2(c.x * side, (c.y + 1) * side),
				new Vector2((c.x + 1) * side, (c.y + 1) * side));

			Poly.Wall left = new Poly.Wall(
				new Vector2(c.x * side, c.y * side),
				new Vector2(c.x * side, (c.y + 1) * side));
			if (!hasLeft && !hasTop && !hasRight && !hasBottom) {
				// ...
				// .х.
				// ...
				w.add(bottom);
				w.add(right);
				w.add(top);
				w.add(left);
			} else if (!hasLeft && !hasRight && !hasBottom) {
				// .x.
				// .х.
				// ...
				w.add(bottom);
				w.add(right);
				w.add(left);
			} else if (!hasTop && !hasRight && !hasBottom) {
				// ...
				// xх.
				// ...
				w.add(bottom);
				w.add(right);
				w.add(top);
			} else if (!hasLeft && !hasTop && !hasBottom) {
				// ...
				// .хx
				// ...
				w.add(bottom);
				w.add(top);
				w.add(left);
			} else if (!hasLeft && !hasTop && !hasRight) {
				// ...
				// .х.
				// .x.
				w.add(right);
				w.add(top);
				w.add(left);
			} else if (!hasLeft && !hasRight) {
				// .x.
				// .х.
				// .x.
				w.add(right);
				w.add(left);
			} else if (!hasTop && !hasBottom) {
				// ...
				// xхx
				// ...
				w.add(bottom);
				w.add(top);
			} else if (!hasRight && !hasBottom) {
				// .x.
				// xх.
				// ...
				w.add(new Poly.Wall(
					new Vector2((c.x + 1) * side, (c.y + 1) * side),
					new Vector2(c.x * side, c.y * side)));
			} else if (!hasLeft && !hasBottom) {
				// .x.
				// .хx
				// ...
				w.add(new Poly.Wall(
					new Vector2((c.x + 1) * side, c.y * side),
					new Vector2(c.x * side, (c.y + 1) * side)));

			} else if (!hasLeft && !hasTop) {
				// ...
				// .хx
				// .x.
				w.add(new Poly.Wall(
					new Vector2(c.x * side, c.y * side),
					new Vector2((c.x + 1) * side, (c.y + 1) * side)));
			} else if (!hasTop && !hasRight) {
				// ...
				// xх.
				// .x.
				w.add(new Poly.Wall(
					new Vector2(c.x * side, (c.y + 1) * side),
					new Vector2((c.x + 1) * side, c.y * side)));
			} else if (!hasRight) {
				// .x.
				// xх.
				// .x.
				w.add(right);
			} else if (!hasTop) {
				// ...
				// xхx
				// .x.
				w.add(top);
			} else if (!hasBottom) {
				// .x.
				// xхx
				// ...
				w.add(bottom);
			} else if (!hasLeft) {
				// .x.
				// .хx
				// .x.
				w.add(left);
			}
			return w;
		}
	}

	public static class Cell {
		public final int x;
		public final int y;
		public boolean wall;

		public Cell(int x, int y, boolean wall) {
			this.x = x;
			this.y = y;
			this.wall = wall;
		}
	}

}
