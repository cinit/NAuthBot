package nil.test.uncertaintycalc.math;

import java.lang.reflect.Array;

public class Utils {

    public static int[] repeatArray(int val, int len) {
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = val;
        }
        return arr;
    }

    public static <T, K> T[] arrayCast(K[] arr, Class<T> type) {
        int len = arr.length;
        T[] ret = (T[]) Array.newInstance(type, len);
        System.arraycopy(arr, 0, ret, 0, len);
        return ret;
    }
}
