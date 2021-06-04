package com.smile.io.nio.copy;

import java.io.File;

/**
 * @author hjw
 * @date 2021/6/2 16:18
 */
public interface FileCopyRunner {

    void copyFile(File source, File target);

}
