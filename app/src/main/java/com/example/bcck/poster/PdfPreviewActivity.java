package com.example.bcck.poster;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bcck.R;

import java.io.File;
import java.io.IOException;

public class PdfPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";

    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;

    private ImageView imagePdf;
    private TextView textPageIndicator;
    private ImageButton btnPrev;
    private ImageButton btnNext;

    private int pageIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);

        imagePdf = findViewById(R.id.imagePdf);
        textPageIndicator = findViewById(R.id.textPageIndicator);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        btnPrev.setOnClickListener(v -> showPage(pageIndex - 1));
        btnNext.setOnClickListener(v -> showPage(pageIndex + 1));

        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (filePath == null || filePath.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy file để xem trước.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            openRenderer(new File(filePath));
            showPage(0);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void openRenderer(File file) throws IOException {
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
    }

    private void showPage(int index) {
        if (pdfRenderer == null) return;
        if (index < 0 || index >= pdfRenderer.getPageCount()) return;

        if (currentPage != null) {
            currentPage.close();
        }

        currentPage = pdfRenderer.openPage(index);
        pageIndex = index;

        int pageWidth = currentPage.getWidth();
        int pageHeight = currentPage.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imagePdf.setImageBitmap(bitmap);

        int pageCount = pdfRenderer.getPageCount();
        textPageIndicator.setText((pageIndex + 1) + " / " + pageCount);

        btnPrev.setEnabled(pageIndex > 0);
        btnNext.setEnabled(pageIndex < pageCount - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentPage != null) {
            currentPage.close();
            currentPage = null;
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
            pdfRenderer = null;
        }
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException ignored) {
            }
            parcelFileDescriptor = null;
        }
    }
}

