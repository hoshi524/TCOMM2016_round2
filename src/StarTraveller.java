import java.util.Arrays;

public class StarTraveller {

	// submit時以外は適当に短く
	private static final int MAX_TIME = 5000;
	private final long endTime = System.currentTimeMillis() + MAX_TIME;

	/*
	 * a = 訪れたstar数
	 * s = star数
	 * p = ships数
	 * t = turn数
	 * u = 訪れているかどうか
	 * f = ufo数
	 */

	int a, s, p, t, f, maxt, dist[][];
	boolean[] u;

	public int init(int[] stars) {
		s = stars.length / 2;
		u = new boolean[s];
		t = 0;
		maxt = stars.length * 2;
		a = 0;
		dist = new int[s][s];
		for (int i = 0; i < s; ++i) {
			for (int j = 0; j < s; ++j) {
				int x = stars[i * 2] - stars[j * 2];
				int y = stars[i * 2 + 1] - stars[j * 2 + 1];
				dist[i][j] = x * x + y * y;
			}
		}
		search = null;
		return 0;
	}

	int[] search;

	public int[] makeMoves(int[] ufos, int[] ships) {
		++t;
		if (t == 1) {
			// first turn
			for (int i = 0; i < ships.length; ++i) {
				if (!u[ships[i]]) {
					u[ships[i]] = true;
					++a;
				}
			}
			f = ufos.length / 3;
			p = ships.length;
			return ships;
		} else if (f > 0 && (s - a) < (maxt - t)) {
			int[] res = Arrays.copyOf(ships, ships.length);
			{
				int fv[] = new int[f];
				for (int i = 0; i < f; ++i) {
					for (int j = 1; j < 3; ++j) {
						if (u[ufos[i * 3 + j]]) continue;
						fv[i] += 0xffffff * (3 - j);
					}
					int p = ufos[i * 3 + 2];
					for (int j = 0; j < s; ++j) {
						if (u[j]) continue;
						fv[i] -= dist[p][j];
					}
				}
				boolean up[] = new boolean[p];
				boolean uf[] = new boolean[f];
				for (int i = 0; i < p; ++i) {
					int to = -1, v = Integer.MIN_VALUE;
					for (int j = 0; j < f; ++j) {
						if (uf[j]) continue;
						if (ships[i] == ufos[j * 3] && v < fv[j]) {
							v = fv[j];
							to = j;
						}
					}
					if (to != -1) {
						uf[to] = true;
						up[i] = true;
						int d =  ufos[to * 3 + 1];
						res[i] = d;
						if (!u[d]) {
							u[d] = true;
							++a;
						}
					}
				}
				for (int i = 0; i < f; ++i) {
					if (uf[i]) continue;
					int ship = -1, v = Integer.MAX_VALUE;
					for (int j = 0; j < p; ++j) {
						if (up[j]) continue;
						if (v > dist[ships[j]][ufos[i * 3 + 1]]) {
							v = dist[ships[j]][ufos[i * 3 + 1]];
							ship = j;
						}
					}
					if (ship != -1) {
						uf[i] = true;
						up[ship] = true;
						int d = ufos[i * 3 + 1];
						res[ship] = d;
						if (!u[d]) {
							u[d] = true;
							++a;
						}
					}
				}
			}
			return res;
		} else {
			int[] res = Arrays.copyOf(ships, ships.length);
			if (search == null) {
				int stars[] = new int[s - a];
				for (int i = 0, si = 0; i < s; ++i) {
					if (!u[i]) {
						stars[si++] = i;
					}
				}
				search = search(ships, stars);
			}
			for (int i = 0; i < p; ++i) {
				for (int t = 0; t < s; ++t) {
					if (ships[i] == search[t] && !u[t]) {
						u[t] = true;
						res[i] = t;
						break;
					}
				}
			}
			return res;
		}
	}

	int[] search(int[] ships, int[] stars) {
		int now[] = new int[ships.length + stars.length], v = 0, rev[] = new int[now.length];
		int dist[][] = new int[now.length][now.length];
		int trans[] = new int[now.length];
		System.arraycopy(ships, 0, trans, 0, ships.length);
		System.arraycopy(stars, 0, trans, ships.length, stars.length);
		for (int i = 0; i < now.length; ++i) {
			for (int j = 0; j < now.length; ++j) {
				dist[i][j] = this.dist[trans[i]][trans[j]];
			}
		}
		Arrays.fill(now, -1);
		Arrays.fill(rev, -1);
		XorShift x = new XorShift();
		{
			int remain[] = new int[now.length], ri = 0;
			int target[] = new int[now.length], ti = 0;
			for (ri = 0; ri < stars.length; ++ri) {
				remain[ri] = ships.length + ri;
			}
			for (ti = 0; ti < ships.length; ++ti) {
				target[ti] = ti;
			}
			while (ri > 0) {
				int d = Integer.MAX_VALUE, from = -1, to = -1;
				for (int i = 0; i < ri; ++i) {
					int t = remain[i];
					for (int j = 0; j < ti; ++j) {
						int f = target[j];
						if (d > dist[t][f]) {
							d = dist[t][f];
							from = i;
							to = j;
						}
					}
				}
				now[target[to]] = remain[from];
				rev[remain[from]] = target[to];
				v += d;
				target[to] = remain[from];
				remain[from] = remain[--ri];
			}
		}
		int best[] = Arrays.copyOf(rev, rev.length), bv = v;
		long remainTime = endTime - System.currentTimeMillis();
		while (remainTime > 0) {
			for (int roop = 0; roop < 0xffff; ++roop) {
				move: {
					int i = ships.length + x.next(stars.length), j = x.next(now.length);
					if (i == j || now[j] == i) break move;
					int tv = v - dist[rev[i]][i] + dist[j][i];
					if (now[i] != -1) {
						tv += dist[rev[i]][now[i]] - dist[i][now[i]];
					}
					if (now[j] != -1) {
						tv += dist[i][now[j]] - dist[j][now[j]];
					}
					if (v > tv) {
						v = tv;
						if (now[i] == -1) {
							now[rev[i]] = -1;
						} else {
							now[rev[i]] = now[i];
							rev[now[i]] = rev[i];
						}
						if (now[j] == -1) {
							now[i] = -1;
						} else {
							now[i] = now[j];
							rev[now[j]] = i;
						}
						now[j] = i;
						rev[i] = j;
					}
				}
				move: {
					int i = ships.length + x.next(stars.length), j = ships.length + x.next(stars.length);
					if (i == j || rev[i] == j || rev[j] == i) break move;
					int a = i, b = j, as = 0, bs = 0;
					while (rev[a] != -1) {
						a = rev[a];
						++as;
					}
					while (rev[b] != -1) {
						b = rev[b];
						++bs;
					}
					if (a == b) {
						a = rev[i];
						b = rev[j];
						int tv = v - dist[a][i] - dist[b][j] + dist[i][j] + dist[a][b];
						if (v > tv) {
							v = tv;
							if (as > bs) {
								int t = rev[i], g = rev[j];
								while (t != g) {
									int n = rev[t];
									int tmp = now[t];
									now[t] = rev[t];
									rev[t] = tmp;
									t = n;
								}
								now[b] = a;
								now[j] = i;
								rev[a] = b;
								rev[i] = j;
							} else {
								int t = rev[j], g = rev[i];
								while (t != g) {
									int n = rev[t];
									int tmp = now[t];
									now[t] = rev[t];
									rev[t] = tmp;
									t = n;
								}
								now[a] = b;
								now[i] = j;
								rev[b] = a;
								rev[j] = i;
							}
						}
					} else {
						a = rev[i];
						b = rev[j];
						int tv = v - dist[a][i] - dist[b][j] + dist[b][i] + dist[a][j];
						if (v > tv) {
							v = tv;
							now[a] = j;
							now[b] = i;
							rev[i] = b;
							rev[j] = a;
						}
					}
				}
			}
			if (bv > v) {
				bv = v;
				System.arraycopy(rev, 0, best, 0, rev.length);
			}
			remainTime = endTime - System.currentTimeMillis();
		}
		int[] res = new int[this.s];
		Arrays.fill(res, -1);
		for (int i = 0; i < best.length; ++i) {
			if (best[i] != -1) res[trans[i]] = trans[best[i]];
		}
		return res;
	}

	private class XorShift {
		int x = 123456789;
		int y = 362436069;
		int z = 521288629;
		int w = 88675123;

		int next(final int n) {
			final int t = x ^ (x << 11);
			x = y;
			y = z;
			z = w;
			w = (w ^ (w >>> 19)) ^ (t ^ (t >>> 8));
			final int r = w % n;
			return r < 0 ? r + n : r;
		}
	}

	private void debug(Object... o) {
		System.out.println(Arrays.deepToString(o));
	}
}
