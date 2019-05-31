package com.example.handlersendmessage;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button btThread;
    private Button btOtherThread;
    private Button btHandlerThread;
    private TextView textView;

    private MyHandler handler;
    private Thread thread;
    private OtherThread otherThread;
    private HandlerThread handlerThread;
    private ThreadHandler threadHandler;

    private int a, b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

        otherThread();
        initHandlerThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 让otherThread的loop停止，这样otherThread线程就可以自动停止了
        Looper looper = otherThread.getLooper();
        looper.quit();
    }

    private void initView() {
        button = findViewById(R.id.bt_click_send_message);
        btThread = findViewById(R.id.bt_click_thread);
        btOtherThread = findViewById(R.id.bt_click_other_thread);
        btHandlerThread = findViewById(R.id.bt_click_handler_thread);
        textView = findViewById(R.id.tv_content);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessageDelayed(0, 1000);
            }
        });

        btThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread = new MyThread(a, b, myThreadCallBack);
                thread.start();
            }
        });

        btOtherThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = Message.obtain();
                message.what = 2;
                message.arg1 = a;
                message.arg2 = b;
                otherThread.handler.sendMessage(message);

                Message message2 = Message.obtain();
                message2.what = 3;
                message2.arg1 = a;
                message2.arg2 = b;
                otherThread.handler.sendMessageDelayed(message2, 2000);
            }
        });

        btHandlerThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadHandler.sendEmptyMessage(4);
            }
        });
    }

    private void initData() {
        a = 3;
        b = 5;

        handler = new MyHandler(new MyHandlerCallBack() {

            @Override
            public void onHandlerCallBack(Message msg) {
                if (msg.what == 0) {
                    textView.setText("收到消息了");
                } else if (msg.what == 1) {
                    textView.setText("a + b = " + msg.arg1);
                } else if (msg.what == 2) {
                    textView.setText("a * b = " + msg.arg1);
                } else if (msg.what == 3) {
                    textView.setText("a * b * 2 = " + (msg.arg1 * 2));
                } else if (msg.what == 4) {
                    textView.setText("收到消息了了了");
                }
            }
        });
    }

    private void otherThread() {
        otherThread = new OtherThread(new OtherHandlerCallBack() {
            @Override
            public void onHandlerCallBack(Message msg) {
                // 接收到数据后计算后返回给主线程显示
                sendMessage(msg.what, msg.arg1 * msg.arg2);
            }
        });
        otherThread.start();
    }

    private MyThreadCallBack myThreadCallBack = new MyThreadCallBack() {
        @Override
        public void onMyCallBack(int result) {
            sendMessage(1, result);
        }
    };

    private void sendMessage(int what, int result) {
        Message message = Message.obtain();
        message.what = what;
        message.arg1 = result;
        handler.sendMessage(message);
    }

    /**
     * 其他线程发消息，main线程接收
     */
    private static class MyThread extends Thread {
        private int a, b;
        private MyThreadCallBack callBack;

        private MyThread(int a, int b) {
            this.a = a;
            this.b = b;
        }

        private MyThread(int a, int b, MyThreadCallBack callBack) {
            this(a, b);
            this.callBack = callBack;
        }

        public void setCallBack(MyThreadCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        public void run() {
            super.run();

            int c = a + b;

            if (callBack != null) {
                callBack.onMyCallBack(c);
            }
        }
    }

    private static class MyHandler extends Handler {
        private MyHandlerCallBack callBack;

        public MyHandler(MyHandlerCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            callBack.onHandlerCallBack(msg);
        }
    }

    /**
     * 在主线程发消息，其他线程接收
     */
    private static class OtherThread extends Thread {
        private OtherHandler handler;
        private static OtherHandlerCallBack handlerCallBack;

        public OtherThread(OtherHandlerCallBack callBack) {
            handlerCallBack = callBack;
        }

        @Override
        public void run() {
            super.run();
            Looper.prepare();

            handler = new OtherHandler();

            Looper.loop();
        }

        public Looper getLooper() {
            return handler.getLooper();
        }

        private static class OtherHandler extends Handler {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                handlerCallBack.onHandlerCallBack(msg);
            }
        }
    }

    /**
     * 使用系统提供的HandlerThread实现
     */
    private void initHandlerThread() {
        handlerThread = new HandlerThread("handler_thread");
        handlerThread.start();

        threadHandler = new ThreadHandler(handlerThread.getLooper(), new OtherHandlerCallBack() {
            @Override
            public void onHandlerCallBack(Message msg) {
                sendMessage(4,-1);
            }
        });
    }

    private static class ThreadHandler extends Handler {

        private OtherHandlerCallBack callBack;

        public ThreadHandler(Looper looper, OtherHandlerCallBack callBack) {
            super(looper);
            this.callBack = callBack;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (callBack != null) {
                callBack.onHandlerCallBack(msg);
            }
        }
    }

    private interface MyThreadCallBack {
        void onMyCallBack(int result);
    }

    private interface MyHandlerCallBack {
        void onHandlerCallBack(Message msg);
    }

    private interface OtherHandlerCallBack {
        void onHandlerCallBack(Message msg);
    }

}
