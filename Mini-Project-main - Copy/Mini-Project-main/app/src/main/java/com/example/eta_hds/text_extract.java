package com.example.eta_hds;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class text_extract extends AppCompatActivity {
    ImageView img;
    Uri imageUri;
    TextView textview;
    Button button;
    Date currentTime;
    private File filePath = null;
    Text.Line textLines;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_ETA_HDS);
        setContentView(R.layout.activity_text_extract);
        img = findViewById(R.id.imageView2);
        textview = findViewById(R.id.textView3);
        button = findViewById(R.id.docx);
        currentTime = Calendar.getInstance().getTime();
        File dir = Environment.getExternalStorageDirectory();

        filePath = new File(Environment.getExternalStorageDirectory(), currentTime.toString()+".docx");
        Toast.makeText(this, ""+filePath, Toast.LENGTH_SHORT).show();


        try {
            if (!filePath.exists()){
                filePath.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();
        String image_path= intent.getStringExtra("imageUri");
        imageUri = Uri.parse(image_path);
        img.setImageURI(imageUri);
        ActivityResultLauncher<String> npermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        return;

                    } else {
                        Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                    }
                });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                npermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                op(getApplicationContext(),imageUri);
            }
        });

    }
    public void op(Context context, Uri uri){
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                if(visionText.getText().equals(".") || visionText.getText().length() == 0){
                                    Toast.makeText(context, "No text found", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    textview.setText(visionText.getText());
                                    Toast.makeText(context, "Document Stored at"+filePath+currentTime.toString()+".docx", Toast.LENGTH_SHORT).show();
                                    testc(visionText);
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "No text found", Toast.LENGTH_SHORT).show();
                                    }
                                });
    }
    public void testc(Text result){
        String texts="";
        for(Text.TextBlock textBlock : result.getTextBlocks()){

            try {
                XWPFDocument xwpfDocument = new XWPFDocument();
                XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph();
                XWPFRun xwpfRun = xwpfParagraph.createRun();
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                for (Text.Line line : textBlock.getLines()) {
                    xwpfRun.setText(line.getText().toString());
                    xwpfRun.setFontSize(11);
                    xwpfRun.addBreak();
                }

                xwpfDocument.write(fileOutputStream);

                if (fileOutputStream!=null){
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                xwpfDocument.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}