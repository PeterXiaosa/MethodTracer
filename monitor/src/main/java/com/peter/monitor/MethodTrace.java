package com.peter.monitor;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.peter.monitor.bean.MethodInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Peter Fu
 * @date 2021/7/2
 */
public class MethodTrace {
    private final static String TAG = "MethodTrace";
    private static final List<Entity> methodList = new LinkedList<>();
    private static boolean mIsTraceMethod = false;

    private final static long COST_TIME = 1000L;

    private final static Object obj = new Object();

    private static final ConcurrentHashMap<String, Entity> map = new ConcurrentHashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void onMethodStart(String name) {
        map.put(name, new Entity(name, System.currentTimeMillis(), true, isInMainThread()));
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public static void onMethodEnd(String name) {
//        Entity entity = map.get(name);
//        if (entity != null) {
//            long nowTime = System.currentTimeMillis();
//            long costTime = nowTime - entity.time;
//            if (costTime > COST_TIME) {
//                Log.d(TAG, " \n【***************************************************\n 方法名(Method Name) : " + name
//                        + ", \n 耗时(Cost Time) : " + costTime + "ms"
//                        +", \n 是否主线程(Is In MainThread) : " + isInMainThread()
//                        + "\n ***************************************************】");
//            }
//            map.remove(name);
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void onMethodEnd(String name, Long value) {
        Entity entity = map.get(name);
        if (entity != null) {
            long nowTime = System.currentTimeMillis();
            long costTime = nowTime - entity.time;
            if (costTime > value) {
                Log.d(TAG, " \n【***************************************************\n 方法名(Method Name) : " + name
                        + ", \n 耗时(Cost Time) : " + costTime + "ms"
                        +", \n 是否主线程(Is In MainThread) : " + isInMainThread()
                        + "\n ***************************************************】");
            }
            map.remove(name);
        }
    }

    public static void startCollect() {
        mIsTraceMethod = true;
    }

    public static void endCollect() {
        mIsTraceMethod = false;
    }

    public static void getCollectMethodCost() {
        new Thread(() -> {
            Log.d(TAG, "thread start to collect");
            List<Entity> tempList;
            synchronized (obj) {
                tempList = new LinkedList<>(methodList);
//                resetTraceManData();
                methodList.clear();
            }
            List<MethodInfo> list = obtainMethodCostData(tempList);
            if (list.size() == 0) {
                Log.d(TAG, "cannot get enough method info");
            } else {
                for (MethodInfo info : list) {
                    if (info.getCostTime() > COST_TIME) {
                        Log.d(TAG, " \n【***************************************************\n method Name : " + info.getName() + ", \n cost time : " + info.getCostTime() + "ms"
                                + "\n ***************************************************】");
                    }
                }
            }
//            resetTraceManData();
            Log.d(TAG, "thread end to collect");
        }).start();
    }

    /**
     * 处理插桩数据，按顺序获取所有方法耗时
     */
    private static List<MethodInfo> obtainMethodCostData(List<Entity> entityList) {
//        synchronized (methodList) {
        Log.d(TAG, "entityList size : " + entityList.size());
        List<MethodInfo> resultList = new ArrayList();
        for (int i = 0; i < entityList.size(); i++) {
            Entity startEntity = entityList.get(i);
            if (!startEntity.isStart) {
                continue;
            }
            startEntity.pos = i;
            Entity endEntity = findEndEntity(entityList, startEntity.name, i + 1);

            if (endEntity != null && endEntity.time - startEntity.time > 0) {
                MethodInfo methodInfo = createMethodInfo(startEntity, endEntity);
                resultList.add(methodInfo);
                if (methodInfo.getCostTime() > COST_TIME) {
                    Log.d(TAG, " \n【***************************************************\n method Name : " + methodInfo.getName() + ", \n cost time : " + methodInfo.getCostTime() + "ms"
                            + "\n ***************************************************】");
                }
            }
        }
        return resultList;
//        }
    }

    /**
     * 找到方法对应的结束点
     *
     * @param name
     * @param startPos
     * @return
     */
    private static Entity findEndEntity(List<Entity> entityList, String name, int startPos) {
        int sameCount = 1;
        for (int i = startPos; i < entityList.size(); i++) {
            Entity endEntity = entityList.get(i);
            if (endEntity.name.equals(name)) {
                if (endEntity.isStart) {
                    sameCount++;
                } else {
                    sameCount--;
                }
                if (sameCount == 0 && !endEntity.isStart) {
                    endEntity.pos = i;
                    return endEntity;
                }
            }
        }
        return null;
    }

    private static MethodInfo createMethodInfo(Entity startEntity, Entity endEntity) {
        return new MethodInfo(startEntity.name,
                endEntity.time - startEntity.time, startEntity.pos, endEntity.pos, startEntity.isMainThread);
    }

    public static void resetTraceManData() {
        synchronized (methodList) {
            methodList.clear();
        }
    }

    private static boolean isInMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    static class Entity {
        public String name;
        public Long time;
        public boolean isStart;
        public int pos;
        public boolean isMainThread;

        public Entity(String name, Long time, boolean isStart, boolean isMainThread) {
            this.name = name;
            this.time = time;
            this.isStart = isStart;
            this.isMainThread = isMainThread;
        }
    }
}
