package ru.yandex.yamblz.ui.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CustomLayout extends ViewGroup {
    private ArrayList<View> mMatchParentChildren = new ArrayList<>();

    public CustomLayout(Context context) {
        this(context, null, 0);
    }

    public CustomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int verticalPadding = getPaddingTop() + getPaddingBottom();
        int totalWidth = 0;
        int layoutWidth = MeasureSpec.getSize(widthMeasureSpec) - horizontalPadding;
        //Высота лейаута по высоте максимального элемента
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec) - verticalPadding;
        int changedHeightMeasureSpec = heightMeasureSpec;

        //Родитель может вызвать onMeasure не единожды
        mMatchParentChildren.clear();


        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            switch (child.getLayoutParams().width) {
                case LayoutParams.MATCH_PARENT:
                    mMatchParentChildren.add(child);
                    break;
                case LayoutParams.WRAP_CONTENT:
                default:
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                    totalWidth += child.getMeasuredWidth();
                    int measuredHeight = child.getMeasuredHeight();
                    if (maxHeight < measuredHeight) {
                        maxHeight = measuredHeight;
                        changedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.UNSPECIFIED);
                    }
            }
        }
        //Если осталось место или оно не ограничено, почему бы не показать matchParent
        boolean haveSpace = (totalWidth < layoutWidth)
                || widthMeasureSpec == MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        //И у нас есть, чем его заполнять
        if ((mMatchParentChildren.size() > 0) && haveSpace) {
            //В случае, если не знаем ширина родителя не остановленна,используем  вдвое увеличенную ширину не match_parent элементов
            layoutWidth = layoutWidth != 0 ? layoutWidth : totalWidth * 2;
            //Остаток разделям между оставшимися детьми
            int mpChildWidth = (layoutWidth - totalWidth) / mMatchParentChildren.size();
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mpChildWidth, MeasureSpec.EXACTLY);
            for (int i = 0; i < mMatchParentChildren.size(); i++) {
                int childHeightMeasureSpec = getChildMeasureSpec(
                        heightMeasureSpec,
                        verticalPadding,
                        mMatchParentChildren.get(i).getLayoutParams().height
                );
                mMatchParentChildren.get(i).measure(
                        childWidthMeasureSpec,
                        childHeightMeasureSpec
                );
                totalWidth += mMatchParentChildren.get(i).getMeasuredWidth();
                int measuredHeight = mMatchParentChildren.get(i).getMeasuredHeight();
                if (maxHeight < measuredHeight) {
                    maxHeight = measuredHeight;
                    changedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                }
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(layoutWidth + horizontalPadding, MeasureSpec.EXACTLY), changedHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int horizontalOffset = getPaddingLeft();
        int verticalOffset = getPaddingTop();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            child.layout(horizontalOffset, verticalOffset, horizontalOffset + childWidth, verticalOffset + childHeight);
            horizontalOffset += childWidth;
        }
    }
}