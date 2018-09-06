/***************************************************************************
 *   Copyright (C) 2018 by AndreyBarmaley  <public.irkutsk@gmail.com>      *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 3 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package my.apps.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class MyDayMonthFactory implements RemoteViewsFactory {
	Context myContext;
	int firstDayOfWeekName;
	int firstDayOfMonthName;
	int daysOfMonth;
	int currentDay;
	int month;
	int year;
	
	public MyDayMonthFactory(Context context, Intent intent) {
		myContext = context;
		firstDayOfWeekName = intent.getIntExtra("firstDayOfWeekName", 0);
		firstDayOfMonthName = intent.getIntExtra("firstDayOfMonthName", 0);
		daysOfMonth = intent.getIntExtra("daysOfMonth", 0);
		currentDay = intent.getIntExtra("currentDay", 0);
		month = intent.getIntExtra("currentMonth", 0);
		year = intent.getIntExtra("currentYear", 0);
	}

	@Override
	public int getCount() {
		return 37; /* force 6 week, fix dynamic height */
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		RemoteViews views = new RemoteViews(myContext.getPackageName(), R.layout.day_month_layout);
		Resources res = myContext.getResources();
		Intent clickIntent = new Intent();
		
		if(position >= getFirstDayOffset() && position < daysOfMonth + getFirstDayOffset()) {
			int date = position - getFirstDayOffset() + 1;
			views.setTextViewText(R.id.textDayMonth, String.valueOf(date));

			// get events
			ContentResolver contentResolver = myContext.getContentResolver();

			long time1 = new GregorianCalendar(year, month, date, 0, 0, 0).getTimeInMillis();
			long time2 = new GregorianCalendar(year, month, date, 23, 59, 59).getTimeInMillis();
		    
			Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();

			ContentUris.appendId(builder, time1);
			ContentUris.appendId(builder, time2);

			Cursor cursorEvent = contentResolver.query(builder.build(), null /* new String[]  { "event_id", "title" } */, null, null, null);

			if(date == currentDay) {
		    		views.setInt(R.id.layoutCell, "setBackgroundColor", res.getColor(R.color.dayMonthCurrentCellBackground));
		    		views.setTextColor(R.id.textDayMonth, res.getColor(R.color.dayMonthCurrentFontColor));
			}
			else
		    		views.setTextColor(R.id.textDayMonth, res.getColor(isSundays(position % 7) ? R.color.dayMonthSundayFontColor : R.color.dayMonthFontColor));
		    
			// set background for event
			if(cursorEvent != null) {
		    		if(cursorEvent.getCount() > 0)
		    			views.setImageViewResource(R.id.viewDayMonth, R.drawable.calendar_event);
		    		cursorEvent.close();
			}
		    
			clickIntent.putExtra("eventYear", year);
			clickIntent.putExtra("eventMonth", month);
			clickIntent.putExtra("eventDay", date);
		} else {
			views.setTextViewText(R.id.textDayMonth, "");
		}
		
		// set click
		views.setOnClickFillInIntent(R.id.layoutCell, clickIntent);

		return views;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataSetChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

	private int getFirstDayOffset() {
		switch(firstDayOfMonthName) {
                        case Calendar.MONDAY:		return firstDayOfWeekName == Calendar.SUNDAY ? 1 : 0;
                        case Calendar.TUESDAY:		return firstDayOfWeekName == Calendar.SUNDAY ? 2 : 1;
                        case Calendar.WEDNESDAY:	return firstDayOfWeekName == Calendar.SUNDAY ? 3 : 2;
                        case Calendar.THURSDAY:		return firstDayOfWeekName == Calendar.SUNDAY ? 4 : 3;
                        case Calendar.FRIDAY:		return firstDayOfWeekName == Calendar.SUNDAY ? 5 : 4;
                        case Calendar.SATURDAY:		return firstDayOfWeekName == Calendar.SUNDAY ? 6 : 5;
                        case Calendar.SUNDAY:		return firstDayOfWeekName == Calendar.SUNDAY ? 0 : 6;
		}
		return 0;
	}

	private boolean isSundays(int pos) {
	    return pos == (firstDayOfWeekName == Calendar.SUNDAY ? 0 : 5) || pos == 6;
	}
}
