/*
 * Copyright (C) 2016 Olmo Gallegos Hernández.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.franctan.pdfviewpager.library.adapter;

import com.franctan.pdfviewpager.library.R;
import com.franctan.pdfviewpager.library.util.SimpleBitmapPool;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URI;


public class PDFPagerAdapter extends PagerAdapter {

    protected String pdfPath;
    protected Context context;
    protected float mRenderQuality;
    protected int mOffScreenSize;
    protected PdfRenderer renderer;
    protected SimpleBitmapPool mBitmapPool;
    protected LayoutInflater inflater;

    int width = 0;
    int height = 0;


    public PDFPagerAdapter(Context context, String pdfPath, float renderQuality, int offScreenSize) {
        this.pdfPath = pdfPath;
        this.context = context;
        mRenderQuality = renderQuality;
        mOffScreenSize = offScreenSize;
        init();
    }

    public void cleanup() {
        mBitmapPool.recycleAll();
        if (renderer != null) {
            renderer.close();
        }
    }

    @SuppressWarnings("NewApi")
    protected void init() {
        try {
            renderer = new PdfRenderer(getSeekableFileDescriptor(pdfPath));
            inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            initBitmapPool();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("PDFPagerAdapter", e.getMessage());
        }
    }

    protected void initBitmapPool() {

        PdfRenderer.Page samplePage = getPDFPage(0);
        width = (int) (samplePage.getWidth() * mRenderQuality);
        height = (int) (samplePage.getHeight() * mRenderQuality);
        samplePage.close();

        mBitmapPool = new SimpleBitmapPool(mOffScreenSize, width, height, Bitmap.Config.ARGB_8888);

    }

    protected ParcelFileDescriptor getSeekableFileDescriptor(String path) throws IOException {
        ParcelFileDescriptor pfd;

        File pdfCopy = new File(path);
        if (pdfCopy.exists()) {
            pfd = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
            return pfd;
        }

        if (isAnAsset(path)) {
            pdfCopy = new File(context.getCacheDir(), path);
            pfd = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
        } else {
            URI uri = URI.create(String.format("file://%s", path));
            pfd = context.getContentResolver().openFileDescriptor(Uri.parse(uri.toString()), "rw");
        }

        return pfd;
    }

    private boolean isAnAsset(String path) {
        return !path.startsWith("/");
    }

    @Override
    @SuppressWarnings("NewApi")
    public Object instantiateItem(ViewGroup container, int position) {
        View v = inflater.inflate(R.layout.view_pdf_page, container, false);
        ImageView iv = (ImageView) v.findViewById(R.id.imageView);

        if (renderer == null || getCount() < position) {
            return v;
        }

        PdfRenderer.Page page = getPDFPage(position);

        Bitmap bitmap = mBitmapPool.getBitmap(position);

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        iv.setImageBitmap(bitmap);
        ((ViewPager) container).addView(v, 0);

        return v;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }


    @SuppressWarnings("NewApi")
    protected PdfRenderer.Page getPDFPage(int position) {
        return renderer.openPage(position);
    }


    @Override
    @SuppressWarnings("NewApi")
    public int getCount() {
        return renderer != null ? renderer.getPageCount() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (View) object;
    }
}
