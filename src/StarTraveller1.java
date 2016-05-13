import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

/*
 * Overall score: 566583.05
 */
public class StarTraveller1 {

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
		return 0;
	}

	public int[] makeMoves(int[] ufos, int[] ships) {
		++t;
		if (t == 1) {
			// first turn
			for (int i = 0; i < ships.length; ++i) {
				u[ships[i]] = true;
			}
			f = ufos.length / 3;
			p = ships.length;
			a = ships.length;
			flow = new MinCostFlow(p + s + 2);
			return ships;
		} else if ((s - a) < (maxt - t) * p) {
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
			int[] go = flow.matching(dist, ships);
			for (int i = 0; i < p; ++i) {
				if (go[i] != -1) {
					res[i] = go[i];
					u[go[i]] = true;
					++a;
				}
			}
			return res;
		}
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
				int dist, v;

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
					for (int i = 0; i < G[v].size(); i++) {
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
				for (int v = 0; v < V; v++) {
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

		int[] matching(int[][] dist, int[] ships) {
			V = p + s - a + 2;
			int source = V - 2, think = V - 1;
			for (int i = 0; i < V; ++i) {
				G[i].clear();
			}
			int trans[] = new int[s], ti = 0;
			for (int i = 0; i < s; ++i) {
				if (u[i]) continue;
				trans[ti++] = i;
			}
			trans = Arrays.copyOf(trans, ti);
			for (int i = 0; i < p; ++i) {
				add_edge(source, i, 1, 0);
			}
			for (int i = 0; i < s - a; ++i) {
				add_edge(p + i, think, 1, 0);
			}
			for (int i = 0; i < p; ++i) {
				for (int j = 0; j < s - a; ++j) {
					add_edge(i, p + j, 1, dist[ships[i]][trans[j]]);
				}
			}
			min_cost_flow(source, think, Math.min(p, s - a));
			int res[] = new int[ships.length];
			Arrays.fill(res, -1);
			for (int i = 0; i < p; ++i) {
				if (go[i] != -1) {
					res[i] = trans[go[i] - p];
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
