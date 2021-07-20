package com.peter.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Peter Fu
 * @date 2021/7/14
 */
public class ConfigNew1 {
    private List<String> UNNEED_TRACE_CLASS = Arrays.asList("R.class", "R$", "Manifest", "BuildConfig");

    private String mTraceConfigFile;

    private HashSet<String> mNeedTracePackageMap = new HashSet<>();

    //在需插桩的包范围内的 无需插桩的白名单
    private HashSet<String> mWhiteClassMap = new HashSet<>();

    //在需插桩的包范围内的 无需插桩的包名
    private HashSet<String> mWhitePackageMap = new HashSet<>();

    private String mBeatClass;

    private boolean mIsNeedLogTraceInfo;

    public boolean isNeedTraceClass(String fileName) {
        boolean isNeed = true;
        if (fileName.endsWith(".class")) {
            for (String unTraceCls : UNNEED_TRACE_CLASS) {
                if (fileName.contains(unTraceCls)) {
                    isNeed = false;
                    break;
                }
            }
        } else {
            isNeed = false;
        }
        return isNeed;
    }

    public boolean isConfigTraceClass(String className) {
        if (mNeedTracePackageMap.isEmpty()) {
            return !(isInWhitePackage(className) || isInWhiteClass(className));
        } else {
            if (isInNeedTracePackage(className)) {
                return !(isInWhitePackage(className) || isInWhiteClass(className));
            } else {
                return false;
            }
        }
    }

    /**
     * 解析插桩配置文件
     */
    public void parseTraceConfigFile() throws FileNotFoundException {
        System.out.println("Begin to parseTraceConfigFile");

        File traceConfigFile = new File(mTraceConfigFile);
        if (!traceConfigFile.exists()) {
            throw new FileNotFoundException("找不到 $mTraceConfigFile 配置文件, 尝试阅读一下 QuickStart。");
        }

        String configStr = UtilsNew1.readFileAsString(traceConfigFile.getAbsolutePath());

        String[] array = configStr.split(System.lineSeparator());
        int index = array.length - 1;
        while (index >= 0) {
            if (array[index].isEmpty()) {
                index--;
            } else {
                break;
            }
        }
        String[] configArray = new String[index+1];
        if (configArray.length >= 0) {
            System.arraycopy(array, 0, configArray, 0, configArray.length);
        }

        for(int i = 0; i < configArray.length; i++) {
            String config = configArray[i];
            if (config == null || config.isEmpty()) {
                continue;
            }
            if (config.startsWith("#")) {
                continue;
            }
            if (config.startsWith("[")) {
                continue;
            }

            if (config.startsWith("-tracepackage ")) {
                config = configStr.replace("-tracepackage ", "");
                mNeedTracePackageMap.add(config);
            } else if (config.startsWith("-keepclass ")) {
                config = configStr.replace("-keepclass ", "");
                mWhiteClassMap.add(config);
            } else if (config.startsWith("-keeppackage ")) {
                config = configStr.replace("-keeppackage ", "");
                mWhitePackageMap.add(config);
            } else if (config.startsWith("-beatclass ")) {
                config = configStr.replace("-beatclass ", "");
                mBeatClass = config;
            }
        }
    }

    private boolean isInNeedTracePackage(String className) {
        for (String it : mNeedTracePackageMap) {
            if (className.contains(it)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInWhitePackage(String className) {
        for (String it : mWhitePackageMap) {
            if (className.contains(it)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInWhiteClass(String className) {
        for (String it : mWhiteClassMap) {
            if (className.equals(it)) {
                return true;
            }
        }
        return false;
    }
}
