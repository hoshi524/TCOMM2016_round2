import java.util.Arrays;

public class StarTraveller {
	int NStars;
	boolean[] used;

	public int init(int[] stars) {
		NStars = stars.length / 2;
		used = new boolean[NStars];
		return 0;
	}

	public int[] makeMoves(int[] ufos, int[] ships) {
		int[] ret = new int[ships.length];
		int retInd = 0;
		for (int i = 0; i < NStars; ++i) {
			if (!used[i]) {
				used[i] = true;
				ret[retInd] = i;
				++retInd;
				if (retInd == ships.length) break;
			}
		}
		while (retInd < ships.length) {
			ret[retInd] = (ships[retInd] + 1) % NStars;
			++retInd;
		}
		return ret;
	}

	void debug(Object... o) {
		System.out.println(Arrays.deepToString(o));
	}
}
