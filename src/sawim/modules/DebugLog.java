package sawim.modules;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import sawim.SawimUI;
import sawim.ui.text.VirtualListModel;
import sawim.ui.text.VirtualList;
import sawim.Sawim;
import sawim.comm.MD5;
import sawim.comm.Util;
import sawim.ui.base.Scheme;
import sawim.util.*;
import ru.sawim.models.form.VirtualListItem;

public final class DebugLog {
    public static final DebugLog instance = new DebugLog();
    private VirtualListModel model = new VirtualListModel();
    private VirtualList list = null;

    public void activate() {
        list = VirtualList.getInstance();
        list.setCaption(JLocale.getString("debug log"));
        list.setModel(model);
        list.setBuildOptionsMenu(new VirtualList.OnBuildOptionsMenu() {
            @Override
            public void onCreateOptionsMenu(Menu menu) {
                menu.add(Menu.FIRST, MENU_COPY_ALL, 2, JLocale.getString("copy_all_text"));
                menu.add(Menu.FIRST, MENU_CLEAN, 2, JLocale.getString("clear"));
            }

            @Override
            public void onOptionsItemSelected(FragmentActivity activity, MenuItem item) {
                switch (item.getItemId()) {
                    case MENU_COPY_ALL:
                        StringBuffer s = new StringBuffer();
                        for (int i = 0; i < list.getModel().elements.size(); ++i) {
                            s.append(list.getModel().elements.get(i).getLabel()).append("\n")
                                    .append(list.getModel().elements.get(i).getDescStr()).append("\n");
                        }
                        SawimUI.setClipBoardText(true, "DebugLog", "", s.toString());
                        break;

                    case MENU_CLEAN:
                        model.clear();
                        list.updateModel();
                        break;
                }
            }
        });
        list.show();
    }

    private static final int MENU_COPY       = 0;
    private static final int MENU_COPY_ALL   = 1;
    private static final int MENU_CLEAN      = 2;

    private void removeOldRecords() {
        final int maxRecordCount = 200;
        while (maxRecordCount < model.getSize()) {
            if (null != list) {
                list.removeFirstText();
            }
        }
    }

    public static void memoryUsage(String str) {
        long size = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        size = (size + 512) / 1024;
        println(str + " = " + size + "kb.");
    }

    private static String _(String text) {
        if (null == text) {
            return "";
        }
        try {
            String text1 = JLocale.getString(text);
            if (!text1.equals(text)) {
                return "[l] " + text1;
            }
        } catch (Exception ignored) {
        }
        return text;
    }
    public static void systemPrintln(String text) {
        System.out.println(text);
    }
    private synchronized void print(String text) {
        VirtualListItem record = model.createNewParser(true);
        String date = Util.getLocalDateString(Sawim.getCurrentGmtTime(), true);
        record.addLabel(date + ": ", Scheme.THEME_MAGIC_EYE_NUMBER,
                Scheme.FONT_STYLE_PLAIN);
        record.addDescriptionSelectable(_(text), Scheme.THEME_TEXT, Scheme.FONT_STYLE_PLAIN);

        model.addPar(record);
        //removeOldRecords();
    }
    public static void println(String text) {
        System.out.println(text);
        instance.print(text);
    }

    public static void panic(String str) {
        try {
            throw new Exception();
        } catch (Exception e) {
            panic(str, e);
        }
    }
    public static void assert0(String str, String result, String heed) {
        assert0(str, (result != heed) && !result.equals(heed));
    }
    public static void assert0(String str, boolean result) {
        if (result) {
            try {
                throw new Exception();
            } catch (Exception e) {
                println("assert: " + _(str));
                e.printStackTrace();
            }
        }
    }

    private long freeMemory() {
        for (int i = 0; i < 10; ++i) {
            sawim.Sawim.gc();
        }
        return Runtime.getRuntime().freeMemory();
    }

    public static void panic(String str, Throwable e) {
        System.err.println("panic: " + _(str));
        String text = "panic: " + _(str) + " "  + e.getMessage()
                + " (" + e.getClass().getName() + ")";
        for (StackTraceElement ste : e.getStackTrace()) {
            text += String.format("\n%s.%s() %d", ste.getClassName(), ste.getMethodName(), ste.getLineNumber());
        }
        println(text);
        e.printStackTrace();
    }

    private static long profilerTime;
    public static long profilerStart() {
        profilerTime = System.currentTimeMillis();
        return profilerTime;
    }
    public static long profilerStep(String str, long startTime) {
        long now = System.currentTimeMillis();
        println("profiler: " + _(str) + ": " + (now - startTime));
        return now;
    }
    public static void profilerStep(String str) {
        long now = System.currentTimeMillis();
        println("profiler: " + _(str) + ": " + (now - profilerTime));
        profilerTime = now;
    }

    public static void startTests() {
        println("1329958015 " + Util.createGmtTime(2012, 02, 23, 4, 46, 55));

        println("TimeZone info");
        java.util.TimeZone tz = java.util.TimeZone.getDefault();
        println("TimeZone offset: " + tz.getRawOffset());
        println("Daylight: " + tz.useDaylightTime());
        println("ID: " + tz.getID());

        MD5 md5 = new MD5();
        md5.init();
        md5.updateASCII("\u0422\u0435\u0441\u0442");
        md5.finish();
        assert0("md5 (ru): failed", !md5.getDigestHex().equals("16497fa0c8e13ce8fab874d959db91b9"));

        md5 = new MD5();
        md5.init();
        md5.updateASCII("Test");
        md5.finish();
        assert0("md5 (en): failed", !md5.getDigestHex().equals("0cbc6611f5540bd0809a388dc95a615b"));

        assert0("bs64decode (0): failed", MD5.decodeBase64(" eg=="), "z");
        assert0("bs64decode (1): failed", MD5.decodeBase64("eg=="), "z");
        assert0("bs64decode (2): failed", MD5.decodeBase64("eno="), "zz");
        assert0("bs64decode (3): failed", MD5.decodeBase64("enp6"), "zzz");
        assert0("bs64decode (4): failed", MD5.decodeBase64(" eg==\n"), "z");
        assert0("bs64decode (5): failed", MD5.decodeBase64("eg==\n"), "z");
        assert0("bs64decode (6): failed", MD5.decodeBase64("eno=\n"), "zz");
        assert0("bs64decode (7): failed", MD5.decodeBase64("enp6\n"), "zzz");
        assert0("bs64 (1): failed", MD5.toBase64(new byte[]{'z'}), "eg==");
        assert0("bs64 (2): failed", MD5.toBase64(new byte[]{'z', 'z'}), "eno=");
        assert0("bs64 (3): failed", MD5.toBase64(new byte[]{'z', 'z', 'z'}), "enp6");

        assert0("replace (1): failed", Util.replace("text2text23", "2", "3"), "text3text33");
        assert0("replace (2): failed", Util.replace("text22text2223", "22", "3"), "text3text323");
        assert0("replace (3): failed", Util.replace("text22text22", "22", "3"), "text3text3");
        assert0("replace (4): failed", Util.replace("text3text33", "22", "3"), "text3text33");
        
    }

    public static void dump(String comment, byte[] data) {
        StringBuffer sb = new StringBuffer();
        sb.append("dump: ").append(comment).append(":\n");
        for (int i = 0; i < data.length; ++i) {
            String hex = Integer.toHexString(((int)data[i]) & 0xFF);
            if (1 == hex.length()) sb.append(0);
            sb.append(hex);
            sb.append(" ");
            if (i % 16 == 15) sb.append("\n");
        }
        println(sb.toString());
    }
}