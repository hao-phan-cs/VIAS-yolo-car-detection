/*  This file has been created by Nataniel Ruiz affiliated with Wall Lab
 *  at the Georgia Institute of Technology School of Interactive Computing
 */

package org.tensorflow.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.tensorflow.demo.Classifier.Recognition;

import java.util.List;

public class BoundingBoxView extends View {
    private String currentLang;
    private Bitmap bitmap;

    private List<Recognition> results;
    private final Paint fgPaint, bgPaint, textPaint, trPaint;

    public BoundingBoxView(final Context context, final AttributeSet set) {
        super(context, set);

        fgPaint = new Paint();
        fgPaint.setColor(getResources().getColor(R.color.colorAccent));
        fgPaint.setStyle(Paint.Style.STROKE);
        fgPaint.setStrokeWidth(4);

        bgPaint = new Paint();
        bgPaint.setARGB(0, 0, 0, 0);
        bgPaint.setAlpha(0);
        bgPaint.setStyle(Paint.Style.STROKE);

        trPaint = new Paint();
        trPaint.setColor(getResources().getColor(R.color.colorAccent));
        trPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(50);  //set text size
    }

    public void setResults(final List<Recognition> results, String lang, Bitmap bitmap) {
        this.results = results;
        this.currentLang = lang;
        this.bitmap = bitmap;
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {

        // Get view size.
        float view_height_temp = (float) this.getHeight();
        float view_width_temp = (float) this.getWidth();
        float view_height = Math.max(view_height_temp, view_width_temp);
        float view_width = Math.min(view_height_temp, view_width_temp);

        String prediction_string = "width: " + Float.toString(view_width) +
                " height: " + Float.toString(view_height);
        Log.v("BoundingBox", prediction_string);

        // Compute multipliers and offsets.
        float INPUT_SIZE = (float) TensorFlowImageListener.getInputSize();
        float size_multiplier_x = view_width / INPUT_SIZE;
        float size_multiplier_y = size_multiplier_x;
        float offset_x = 0;
        float offset_y = (view_height - INPUT_SIZE * size_multiplier_y) / 2;

        if (results != null) {
            for (final Recognition recog : results) {

                boolean showResult = false;

                    String name = recog.getTitle();
                    float score = recog.getConfidence();

                    if (name.contains("xe ôtô")
                            || name.contains("xe buýt")
                            || name.contains("car")
                            || name.contains("bus")) {
                            showResult = true;
                    } else if (name.contains("xe máy")
                            || name.contains("motorbike")) {
                            showResult = true;
                    } else if (name.contains("xe đạp")
                            || name.contains("bicycle")){
                            showResult = true;
                    }

                    if (showResult) {

                    // Create class name text on bounding box.
                    String class_name = recog.getTitle();
                        if (!currentLang.equals("vi")) {
                            switch (class_name) {
                                case "xe ôtô":
                                    class_name = "car";
                                    break;
                                case "xe buýt":
                                    class_name = "bus";
                                    break;
                                case "xe đạp":
                                    class_name = "bicycle";
                                    break;
                                case "xe máy":
                                    class_name = "motorbike";
                                    break;
                            }
                        }

                        // Get x, y, width and height before pre processing of
                        // bounding boxes. Then pre-process the bounding boxes
                        // by using the multipliers and offsets to map a 448x448 image
                        // coordinates to a device_width x device_height surface
                        RectF preBoundingBox = recog.getLocation();
                        float bounding_x = preBoundingBox.left;
                        float bounding_y = preBoundingBox.top;
                        float box_width = preBoundingBox.right;
                        float box_height = preBoundingBox.bottom;

                        //String d = getDistance(class_name, 448, (int)bounding_x, (int)bounding_y, (int)box_width, (int)box_height);

                        bounding_x *= size_multiplier_x;
                        bounding_y *= size_multiplier_y;
                        bounding_x += offset_x;
                        bounding_y += offset_y;

                        box_width *= size_multiplier_x;
                        box_height *= size_multiplier_y;
                        bounding_x -= box_width;
                        bounding_y -= box_height;

                        float bounding_x2 = bounding_x + 2 * box_width;
                        float bounding_y2 = bounding_y + 2 * box_height;

                        // Create new bounding box and draw it.
                        RectF boundingBox = new RectF(bounding_x, bounding_y, bounding_x2, bounding_y2);

                        canvas.drawRect(boundingBox, fgPaint);
                        canvas.drawRect(boundingBox, bgPaint);

                    float text_width = textPaint.measureText(class_name) / 2;
                    float text_size = textPaint.getTextSize();
                    float text_center_x = bounding_x - 2;
                    float text_center_y = bounding_y - text_size;
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawRect(text_center_x, text_center_y, text_center_x + 2 * text_width, text_center_y + text_size, trPaint);
                    canvas.drawText(class_name, text_center_x + text_width, text_center_y + text_size, textPaint);

                }
            }
        }
    }

    String getDistance(String name, int size, int b_x, int b_y, int b_w, int b_h){

        b_x *= bitmap.getWidth();
        b_y *= bitmap.getHeight();

        b_h = b_h*bitmap.getHeight();
        b_w = b_w*bitmap.getWidth();

        Bitmap objectBitmap = Bitmap.createBitmap(bitmap, b_w, b_h, b_x-b_w, b_y-b_h);
        int pixels = objectBitmap.getByteCount();
        float focalLength = getFocalLength();
        float w = 2;
        switch (name) {
            case "xe ôtô":
                w = 2;
                break;
            case "xe buýt":
                w = 3;
                break;
            case "xe máy":
                w = 0.5f;
                break;
        }
        w = (float) (w*100/2.54);
        float distance = focalLength*w/pixels;
        distance = (float) (distance*2.54/100);
        String d = String.valueOf(distance);
        //d = d.substring(d.indexOf(".", 2));
        return d;
    }

    float getFocalLength(){
        Camera camera = Camera.open();
        return camera.getParameters().getFocalLength();
    }
}
