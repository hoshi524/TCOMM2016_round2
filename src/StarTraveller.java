import java.util.Arrays;

public class StarTraveller {

	/*
	 * a = 訪れたstar数
	 * s = star数
	 * p = ships数
	 * t = turn数
	 * u = 訪れているかどうか
	 * f = ufo数
	 */

	int stars[], a, s, p, t, f, maxt;
	boolean[] u;

	public int init(int[] stars) {
		maxt = stars.length * 2;
		this.stars = stars;
		s = stars.length / 2;
		u = new boolean[s];
		t = 0;
		a = 0;
		return 0;
	}

	public int[] makeMoves(int[] ufos, int[] ships) {
		if (t == 0) {
			++t;
			// first turn
			for (int i = 0; i < ships.length; ++i) {
				u[ships[i]] = true;
			}
			f = ufos.length / 3;
			p = ships.length;
			a = ships.length;
			return ships;
		} else {
			++t;
			if ((s - a) < (maxt - t) * p) {
				int[] ret = Arrays.copyOf(ships, ships.length);
				for (int i = 0, min = Math.min(p, f); i < min; ++i) {
					int d = ufos[i * 3 + 1];
					ret[i] = d;
					if (!u[d]) {
						u[d] = true;
						++a;
					}
				}
				return ret;
			} else {
				int[] ret = new int[ships.length];
				int retInd = 0;
				for (int i = 0; i < s; ++i) {
					if (!u[i]) {
						u[i] = true;
						ret[retInd] = i;
						++retInd;
						if (retInd == ships.length) break;
					}
				}
				while (retInd < ships.length) {
					ret[retInd] = (ships[retInd] + 1) % s;
					++retInd;
				}
				return ret;
			}
		}
	}

	private void debug(Object... o) {
		System.out.println(Arrays.deepToString(o));
	}
}
