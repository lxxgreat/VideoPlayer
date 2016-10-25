package com.shane.android.videoplayer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.shane.android.videoplayer.util.CoderUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    String coder = "lxxgreat";
    String decoder = "bHh4Z3JlYXQ=";
    String DECODE_KEY = "d101b17c77ff93cs";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

//        System.out.println(CoderUtil.base64AesDecode(coder, DECODE_KEY));
        assertEquals("com.shane.android.videoplayer", appContext.getPackageName());
    }
}
