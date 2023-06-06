package com.start.apps.pheezee.activities;

import static android.util.DisplayMetrics.DENSITY_MEDIUM;
import static com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections.*;
import static com.facebook.FacebookSdk.getApplicationContext;

import static start.apps.pheezee.R.id.iv_back_app_info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.animation.content.Content;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import start.apps.pheezee.R;

public class BluetoothScreen extends AppCompatActivity {

    Button print_button, print_thermal;
    File pdfFile;

    PDFView pdfView;
    Bitmap[] bitmaps;

    Bitmap resizedBitmap;

    String path;

    PopupWindow popup;

    Bitmap decodedByte;

    private int progress = 5;

    PdfRenderer renderer = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_screen);
        print_button = findViewById(R.id.button_bluetooth);
        pdfView = findViewById(R.id.pdfView);
        ImageView button1 = findViewById(iv_back_app_info);
        print_thermal = findViewById(R.id.print_thermal);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Intent patientId = getIntent();
        String patientid = patientId.getStringExtra("patientId");
        Intent date = getIntent();
        String date_value =  date.getStringExtra("date");

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.progress_bar_notification);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        ProgressBar mCustomProgressBar  = dialog.findViewById(R.id.CustomProgressBar);
        TextView fact_text = dialog.findViewById(R.id.fact_text);
        String[] myStringArray = {"Checking joint ROM helps assess flexibility and find ways to improve.", "EMG can identify muscle weakness and imbalances in patients.", "Joint ROM exercises can improve mobility, flexibility, and prevent problems.", "EMG can show patients how to activate muscles correctly during exercises.", "Joint ROM exercises can help prevent injuries and improve sports performance."};
        Random random = new Random();
        int randomIndex = random.nextInt(myStringArray.length);
        String randomValue = myStringArray[randomIndex];
        fact_text.setText(randomValue.toString());
//        Log.d("MyApp", "My random value is " + randomValue);




        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                dialog.show();
                progress++;
                mCustomProgressBar.setProgress((int) progress * 100 / (60000 / 1000));
                Drawable draw = ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_progressbar);
                mCustomProgressBar.setProgressDrawable(draw);
            }

            @Override
            public void onFinish() {
                 Log.e("555555555555555555555","88888888888");
                path = "/storage/sdcard0/Android/data/com.start.apps.pheezee/files/Pheezee/"+patientid+date_value+".pdf";
                dialog.dismiss();
                pdfView = (PDFView) findViewById(R.id.pdfView);
                pdfView.fromFile(new File(getApplicationContext().getExternalFilesDir(null)+"/Pheezee", patientid+date_value+".pdf"))
                        .load();
            }
        }.start();

        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("Hello 5555555555555555555555");
                if(popup != null) {
                    popup.dismiss();
                }
            }
        });

        print_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {

                if(BluetoothPrintersConnections.selectFirstPaired() == null){
                    popup = new PopupWindow(BluetoothScreen.this);
                    View customView = LayoutInflater.from(BluetoothScreen.this).inflate(R.layout.buyprinter, null);
                    popup.setContentView(customView);
                    int width = 800;
                    int height = 400;
                    popup.setWidth(width);
                    popup.setHeight(height);
                    int location[] = new int[2];
                    int x = location[0] + customView.getWidth();
                    int y = location[1] + 50; // add 50 pixels from the top
                    popup.showAtLocation(customView, Gravity.TOP | Gravity.RIGHT, x, 250);
                    TextView buy_now_ptr = customView.findViewById(R.id.buy_now_ptr);
                    buy_now_ptr.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=919398772387&text=Hello, Could you please assist me in purchasing a Bluetooth printer to use with the app? \u2028Thank you!")));
                        }
                    });
                }

                if(BluetoothPrintersConnections.selectFirstPaired() != null) {
                    popup = new PopupWindow(BluetoothScreen.this);
                    View customView = LayoutInflater.from(BluetoothScreen.this).inflate(R.layout.printerlayour, null);
                    popup.setContentView(customView);
                    int width = 700;
                    int height = 500;
                    popup.setWidth(width);
                    popup.setHeight(height);
                    int location[] = new int[2];
                    int x = location[0] + customView.getWidth();
                    int y = location[1] + 50; // add 50 pixels from the top
                    popup.showAtLocation(customView, Gravity.TOP | Gravity.RIGHT, x, 250);

                    LinearLayout thermal_printer = customView.findViewById(R.id.thermal_printer);

                    LinearLayout printer = customView.findViewById(R.id.other_printer);
                    printer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new CountDownTimer(3000, 1000) {
                                @Override
                                public void onTick(long l) {
                                    pdfView.fromFile(new File(getApplicationContext().getExternalFilesDir(null)+"/Pheezee", patientid+date_value+".pdf"))
                                            .load();
                                }

                                @Override
                                public void onFinish() {
                                    File file = new File(getApplicationContext().getExternalFilesDir(null)+"/Pheezee", patientid+date_value+".pdf");
                                    Uri fileUri = FileProvider.getUriForFile(BluetoothScreen.this, getApplicationContext().getPackageName() + ".my.package.name.provider", file);
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("application/pdf");
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                                    startActivity(Intent.createChooser(shareIntent, "Share file"));
                                }
                            }.start();

                        }
                    });
                    thermal_printer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Open the PDF file

                            new CountDownTimer(3000, 1000) {
                                @Override
                                public void onTick(long l) {
                                    pdfView.fromFile(new File(getApplicationContext().getExternalFilesDir(null)+"/Pheezee", patientid+date_value+"thermal_printer"+".pdf"))
                                            .load();
                                }

                                @Override
                                public void onFinish() {
                                    try {
                                        renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(getApplicationContext().getExternalFilesDir(null)+"/Pheezee", patientid+date_value+"thermal_printer"+".pdf"), ParcelFileDescriptor.MODE_READ_ONLY));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    // Create an array to hold the bitmaps
                                    Bitmap[] bitmaps = new Bitmap[renderer.getPageCount()];


                                    // Render each page into a bitmap
                                    for (int i = 0; i < renderer.getPageCount(); i++) {
                                        Log.e("111111111111111111111111", String.valueOf(i));
                                        PdfRenderer.Page page = renderer.openPage(i);
                                        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                        ColorMatrix matrix = new ColorMatrix();
                                        matrix.setSaturation(0);
                                        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                                        Paint paint = new Paint();
                                        paint.setColorFilter(filter);
                                        Canvas canvas = new Canvas(bitmap);
                                        canvas.drawBitmap(bitmap, 0, 0, paint);
                                        bitmaps[i] = bitmap;
                                        decodedByte = bitmap;
                                        Log.e("222222222222222222222", String.valueOf(decodedByte));
                                        try {
                                            Log.e("33333333333333333333333", "333333333333333333333333");
                                            EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 70f, 48);
                                            int width = decodedByte.getWidth(), height = decodedByte.getHeight();
                                            StringBuilder textToPrint = new StringBuilder();
                                            for (int y = 0; y < height; y += 256) {
                                                resizedBitmap = Bitmap.createBitmap(decodedByte, 0, y, width, (y + 256 >= height) ? height - y : 256);
                                                textToPrint.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, resizedBitmap) + "</img>\n");
                                            }
                                            printer.printFormattedTextAndCut(textToPrint.toString());
                                            decodedByte.recycle();
                                            page.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    // Clean up resources
                                    renderer.close();
                                }
                            }.start();












                        }
                    });

                }








            }

        });

//        print_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                int pageCount = pdfView.getPageCount();
//                bitmaps = new Bitmap[pageCount];
//                for (int i = 0; i < pageCount; i++) {
//                    pdfView.jumpTo(i);
//                    pdfView.setDrawingCacheEnabled(true);
//                    pdfView.buildDrawingCache();
//                    Bitmap bitmap = Bitmap.createBitmap(pdfView.getDrawingCache());
//                    bitmaps[i] = bitmap;
//                    pdfView.setDrawingCacheEnabled(true);
//                    Bitmap decodedByte = bitmaps[i];
//
//
//
//
//                    new Thread(new Runnable() {
//                        public void run() {
//                            try {
//                                Log.i("Status_Bluetooh", String.valueOf(BluetoothPrintersConnections.selectFirstPaired()));
//                                EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 70f, 48);
//                                int width = decodedByte.getWidth(), height = decodedByte.getHeight();
//
//                                StringBuilder textToPrint = new StringBuilder();
//                                for(int y = 0; y < height; y += 256) {
//                                    resizedBitmap = Bitmap.createBitmap(decodedByte, 0, y, width, (y + 256 >= height) ? height - y : 256);
//                                    textToPrint.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, resizedBitmap) + "</img>\n");
//                                    Log.e("Status_Device", "three_loop");
//                                }
////                                resizedBitmap.recycle();
//                                decodedByte.recycle();
//                                Log.e("Status_Device", "four_loop");
//                                printer.printFormattedTextAndCut(textToPrint.toString());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
//                }
//
//            }
//        });



    }



}