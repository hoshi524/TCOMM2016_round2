import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class StarTraveller {

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
	MinCostFlow flow;

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
			flow = new MinCostFlow(p + s + 2);
			return ships;
		} else if (f > 0 && (s - a) < (maxt - t)) {
			int[] res = Arrays.copyOf(ships, ships.length);
			for (int i = 0, min = Math.min(p, f); i < min; ++i) {
				int d = ufos[i * 3 + 1];
				res[i] = d;
				if (!u[d]) {
					u[d] = true;
					++a;
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
		int best[] = Arrays.copyOf(rev, rev.length), bv = v, count = 0, limit = now.length * now.length;
		for (int roop = 0; roop < 0xffffff; ++roop) {
			int i = ships.length + x.next(stars.length), j = x.next(now.length);
			if (i == j || now[j] == i) continue;
			int tv = v - dist[rev[i]][i] + dist[j][i];
			if (now[i] != -1) {
				tv += dist[rev[i]][now[i]] - dist[i][now[i]];
			}
			if (now[j] != -1) {
				tv += dist[i][now[j]] - dist[j][now[j]];
			}
			if (v > tv || count > limit) {
				count = 0;
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
				if (bv > v) {
					bv = v;
					System.arraycopy(rev, 0, best, 0, rev.length);
				}
			} else {
				++count;
			}
		}
		int[] res = new int[this.s];
		Arrays.fill(res, -1);
		for (int i = 0; i < best.length; ++i) {
			if (best[i] != -1) res[trans[i]] = trans[best[i]];
		}
		return res;
	}

	private class MinCostFlow {
		class edge {
			int to, cap, cost, rev;

			edge(int to, int cap, int cost, int rev) {
				this.to = to;
				this.cap = cap;
				this.cost = cost;
				this.rev = rev;
			}
		}

		int V;
		ArrayList<edge> G[];
		int h[];
		int dist[];
		int prevv[], preve[];
		int go[];

		public MinCostFlow(int V) {
			this.V = V;
			G = new ArrayList[V];
			for (int i = 0; i < V; i++)
				G[i] = new ArrayList<edge>();
			h = new int[V];
			dist = new int[V];
			prevv = new int[V];
			preve = new int[V];
			go = new int[V];
		}

		void add_edge(int from, int to, int cap, int cost) {
			G[from].add(new edge(to, cap, cost, G[to].size()));
			G[to].add(new edge(from, 0, -cost, G[from].size() - 1));
		}

		int min_cost_flow(int s, int t, int f) {
			class pair implements Comparable<pair> {
				final int dist, v;

				pair(int dist, int v) {
					this.dist = dist;
					this.v = v;
				}

				@Override
				public int compareTo(pair paramT) {
					return dist - paramT.dist;
				}
			}
			int res = 0;
			Arrays.fill(h, 0);
			Arrays.fill(go, -1);
			PriorityQueue<pair> queue = new PriorityQueue<pair>();
			while (f > 0) {
				Arrays.fill(dist, Integer.MAX_VALUE);
				dist[s] = 0;
				queue.add(new pair(0, s));
				while (!queue.isEmpty()) {
					pair p = queue.poll();
					int v = p.v;
					if (dist[v] < p.dist) continue;
					for (int i = 0; i < G[v].size(); ++i) {
						edge e = G[v].get(i);
						if (e.cap == 0) continue;
						int d = dist[v] + e.cost + h[v] - h[e.to];
						if (dist[e.to] > d) {
							dist[e.to] = d;
							prevv[e.to] = v;
							preve[e.to] = i;
							queue.add(new pair(d, e.to));
						}
					}
				}
				if (dist[t] == Integer.MAX_VALUE) return -1;
				for (int v = 0; v < V; ++v) {
					h[v] += dist[v];
				}
				int d = f;
				for (int v = t; v != s; v = prevv[v]) {
					d = Math.min(d, G[prevv[v]].get(preve[v]).cap);
				}
				f -= d;
				res += d * h[t];
				for (int v = t; v != s; v = prevv[v]) {
					edge e = G[prevv[v]].get(preve[v]);
					go[prevv[v]] = v;
					e.cap -= d;
					G[v].get(e.rev).cap += d;
				}
			}
			return res;
		}

		int[] matching(int[][] dist, int[] ships, int[] stars) {
			V = p + s - a + 2;
			int source = V - 2, think = V - 1;
			for (int i = 0; i < V; ++i) {
				G[i].clear();
			}
			for (int i = 0; i < p; ++i) {
				add_edge(source, i, 1, 0);
			}
			for (int i = 0; i < s - a; ++i) {
				add_edge(p + i, think, 1, 0);
			}
			for (int i = 0; i < p; ++i) {
				for (int j = 0; j < s - a; ++j) {
					add_edge(i, p + j, 1, dist[ships[i]][stars[j]]);
				}
			}
			min_cost_flow(source, think, Math.min(p, s - a));
			int res[] = new int[ships.length];
			Arrays.fill(res, -1);
			for (int i = 0; i < p; ++i) {
				if (go[i] != -1) {
					res[i] = stars[go[i] - p];
				}
			}
			return res;
		}
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
