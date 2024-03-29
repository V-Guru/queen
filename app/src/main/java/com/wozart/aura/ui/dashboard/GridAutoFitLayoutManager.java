package com.wozart.aura.ui.dashboard;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridAutoFitLayoutManager extends GridLayoutManager {

  private int columnWidth;
  private boolean columnWidthChanged = true;

  public GridAutoFitLayoutManager(Context context, int columnWidth) {
    super(context, 1);
    this.columnWidth = columnWidth;
    setColumnWidth(columnWidth);
  }

  public GridAutoFitLayoutManager(Context context, int columnWidth, int orientation,
                                  boolean reverseLayout) {
    super(context, 1, orientation, reverseLayout);

    setColumnWidth(columnWidth);
  }

  public void setColumnWidth(int newColumnWidth) {
    if (newColumnWidth > 0 && newColumnWidth != columnWidth) {
      columnWidth = newColumnWidth;
      columnWidthChanged = true;
    }
  }

  @Override
  public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    if (columnWidthChanged && columnWidth > 0) {
      int totalSpace;
      if (getOrientation() == VERTICAL) {
        totalSpace = getWidth() - getPaddingRight() - getPaddingLeft();
      } else {
        totalSpace = getHeight() - getPaddingTop() - getPaddingBottom();
      }
      int spanCount = Math.max(1, totalSpace / columnWidth);
      setSpanCount(spanCount);
      columnWidthChanged = false;
    }
    super.onLayoutChildren(recycler, state);
  }
}