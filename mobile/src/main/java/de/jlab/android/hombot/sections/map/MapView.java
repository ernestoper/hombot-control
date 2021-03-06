package de.jlab.android.hombot.sections.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.common.core.HombotMap;

/**
 * Created by frede_000 on 06.10.2015.
 */
public class MapView extends View {

    public enum LayerType {
        ABYSS,
        BLOCK,
        BUMP,
        BUMP_ABYSS,
        FIGHT,
        FLOOR,
        MOVE_OBJECT,
        SNEAKING,
        SCREWING,
        UNDETERMINED,
        VOID,
        WALL
    }

    private MapDrawable mMap;
    private float mZoom = 2;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Canvas mMapCanvas;
    private Bitmap mMapBitmap;
    private Handler mDrawHandler = new Handler();
    private Runnable mDrawRunnable = new Runnable() {

        @Override
        public void run() {

            if (mMapBitmap == null) {
                mMapBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            }
            if (mMapCanvas == null) {
                mMapCanvas = new Canvas(mMapBitmap);
            }
            mMapCanvas.drawColor(getContext().getResources().getColor(R.color.map_background));

            if (mMap == null)
                return;

            for (MapViewLayer layer : mLayers.values()) {
                layer.draw(mMapCanvas, mPaint, mZoom);
            }
        }
    };

    private LinkedHashMap<LayerType, MapViewLayer> mLayers = new LinkedHashMap<>();

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void toggleLayer(LayerType type) {
        MapViewLayer layer = mLayers.get(type);
        layer.setEnabled(!layer.isEnabled());
        redraw();
        invalidate();
    }

    public boolean isLayerVisible(LayerType type) {
        MapViewLayer layer = mLayers.get(type);
        return layer.isEnabled();
    }

    private void setLayer(LayerType layerType, ArrayList<? extends MapDrawable.MapDrawableItem> items, int color) {
        MapViewLayer layer = mLayers.get(layerType);
        if (layer == null) {
            layer = new MapViewLayer(items, color);
            mLayers.put(layerType, layer);
        } else {
            layer.setItems(items);
            layer.setPrimaryColor(color);
        }
    }

    public void setMap(final HombotMap map) {
        mMap = MapDrawable.convert(map);
        post(new Runnable() {
            @Override
            public void run() {
                // RENDER FLOOR
                setLayer(LayerType.FLOOR, mMap.getCells(MapDrawable.CellType.FLOOR), getContext().getResources().getColor(R.color.map_type_floor));
                setLayer(LayerType.WALL, mMap.getCells(MapDrawable.CellType.WALL), getContext().getResources().getColor(R.color.map_type_wall));

                // RENDER ADDITIONAL INFO
                setLayer(LayerType.SNEAKING, mMap.getCells(MapDrawable.CellType.SNEAKING), getContext().getResources().getColor(R.color.map_flag_sneak));
                setLayer(LayerType.SCREWING, mMap.getCells(MapDrawable.CellType.SCREWING), getContext().getResources().getColor(R.color.map_flag_screw));
                setLayer(LayerType.BUMP, mMap.getCells(MapDrawable.CellType.BUMP), getContext().getResources().getColor(R.color.map_flag_bump));
                setLayer(LayerType.ABYSS, mMap.getCells(MapDrawable.CellType.ABYSS), getContext().getResources().getColor(R.color.map_flag_abyss));
                setLayer(LayerType.BUMP_ABYSS, mMap.getCells(MapDrawable.CellType.ABYSS), getContext().getResources().getColor(R.color.map_flag_bump_abyss));
                setLayer(LayerType.MOVE_OBJECT, mMap.getCells(MapDrawable.CellType.MOVE_OBJECT), getContext().getResources().getColor(R.color.map_flag_move_object));
                setLayer(LayerType.FIGHT, mMap.getCells(MapDrawable.CellType.FIGHT), getContext().getResources().getColor(R.color.map_flag_fight));
                setLayer(LayerType.UNDETERMINED, mMap.getCells(MapDrawable.CellType.FIGHT), getContext().getResources().getColor(R.color.map_flag_undetermined));

                // RENDER GRID
                setLayer(LayerType.BLOCK, mMap.blocks, Color.DKGRAY);

                int mWidth = map.getOffsets().xMax - map.getOffsets().xMin + 1;
                int mHeight = map.getOffsets().yMax - map.getOffsets().yMin + 1;
                float factor = Math.min(getMeasuredWidth() / mWidth, getMeasuredHeight() / mHeight);
                mZoom = factor / 10;

                redraw();
                invalidate();
            }
        });
    }

    public void zoomIn() {
        mZoom++;
        redraw();
        invalidate();
    }

    public void zoomOut() {
        if (mZoom > 1) {
            mZoom--;
            redraw();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);
        if (mMapBitmap != null) {
            canvas.drawBitmap(mMapBitmap, 0, 0, mPaint);
        }

        /*
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(mZoom);

        canvas.drawColor(Color.TRANSPARENT);

        // canvas.drawLine(1f, 1f, 200f, 200f, paint);

        if (mMap == null)
            return;

        for (MapViewLayer layer : mLayers.values()) {
            layer.draw(canvas, paint, mZoom);
        }
        */
    }

    private void redraw() {
        mDrawHandler.removeCallbacks(mDrawRunnable);
        mDrawHandler.post(mDrawRunnable);
    }

      ///////////////////////
     /// SCROLLING /////////
    ///////////////////////

    // TODO IMPLEMENT SCROLLING AND ZOOMING VIA https://developer.android.com/training/gestures/scroll.html

}
