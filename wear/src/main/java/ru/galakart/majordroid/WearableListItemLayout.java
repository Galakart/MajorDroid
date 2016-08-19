package ru.galakart.majordroid;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by alex.lavrinovich on 02-May-16.
 */
public class WearableListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private ImageView img;
    private TextView name;
    private TextView description;

    private final float mFadedTextAlpha;
    private final int mFadedCircleColor;
    private final int mChosenCircleColor;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        mFadedTextAlpha = 20 / 100f;
        mFadedCircleColor = getResources().getColor(R.color.grey);
        mChosenCircleColor = getResources().getColor(R.color.blue);
    }

    // Get references to the icon and text in the item layout definition
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // These are defined in the layout file for list items
        // (see next section)
        img = (ImageView) findViewById(R.id.circle);
        name = (TextView) findViewById(R.id.name);
        description = (TextView) findViewById(R.id.description);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        name.setAlpha(1f);
        description.setAlpha(1f);

        ((GradientDrawable) img.getDrawable()).setColor(mChosenCircleColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        ((GradientDrawable) img.getDrawable()).setColor(mFadedCircleColor);
        name.setAlpha(mFadedTextAlpha);
        description.setAlpha(mFadedTextAlpha);
    }
}
