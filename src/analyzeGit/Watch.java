package analyzeGit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 時間計測用クラス
 *
 * @author phithon
 *
 */
public class Watch {
    /** 計測開始時間 */
    private final long start;
    /** 最後にラップを計った時間 */
    private long checked;
    /** 処理ごとの経過時間 */
    private Map<String, Long> laps = new LinkedHashMap<String, Long>();

    /**
     * 現在時刻を計測開始時刻として、時間計測用オブジェクトを作成します。
     */
    public Watch() {
        start = checked = System.currentTimeMillis();
    }

    /**
     * 計測開始時刻からの経過時間を返します。
     *
     * @return 計測開始時刻からの経過時間
     */
    public long fromStart() {
        long now = System.currentTimeMillis();
        return now - start;
    }

    /**
     * 現在時刻を記録し、前回の記録からの経過時間を返します。
     *
     * @return 前回の記録からの経過時間
     */
    public long check() {
        long now = System.currentTimeMillis();
        long diff = now - checked;
        checked = now;
        return diff;
    }

    /**
     * 現在時刻を記録し、前回の記録からの経過時間を返します。
     * 更に、指定した名前ごとの累積記録を保存します。
     *
     * @param lapName 保存する名前
     * @return 前回の記録からの経過時間
     */
    public long check(String lapName) {
        long now = System.currentTimeMillis();
        long diff = now - checked;
        if (laps.containsKey(lapName)) {
            laps.put(lapName, laps.get(lapName) + diff);
        } else {
            laps.put(lapName, diff);
        }
        checked = now;
        return diff;
    }

    /**
     * long型を時間表現に直します。
     *
     * @param time 時間を表すlong形
     * @return XX:XX:XX.XXX形式の文字列
     */
    public static String format(long time) {
        int milli = (int) time % 1000;
        time /= 1000;
        int second = (int) time % 60;
        time /= 60;
        int minute = (int) time % 60;
        int hour = (int) time / 60;
        return String.format("%02d:%02d:%02d.%03d", hour, minute, second, milli);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Long> lap : laps.entrySet()) {
            sb.append(lap.getKey());
            sb.append(" : ");
            sb.append(Watch.format(lap.getValue()));
            sb.append("\n");
        }
        sb.append(format(fromStart()));
        return sb.toString();
    }

    public static void main(String[] args) {
        Watch watch = new Watch();
        for (int i = 0; i < 10000; i++) {
            List<Integer> numbers = new ArrayList<Integer>();
            for (int j = 0; j < 100000; j++) {
                numbers.add(j);
            }
            watch.check("add"); // 追加するのにかかった時間を記録
            Collections.shuffle(numbers);
            watch.check("shuffle"); // シャッフルするのにかかった時間を記録
            numbers.clear();
            watch.check("clear"); // クリアするのにかかった時間を記録
        }
        // 各処理の実行時間を確認
        System.out.println(watch);
    }
}