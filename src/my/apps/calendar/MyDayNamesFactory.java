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

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class MyDayNamesFactory implements RemoteViewsFactory {
	Context myContext;
	String shortDays[];
	int firstDayOfWeekName;

	public MyDayNamesFactory(Context context, Intent intent) {
		myContext = context;
		shortDays = intent.getStringArrayExtra("shortDays");
		firstDayOfWeekName = intent.getIntExtra("firstDayOfWeekName", 0);
	}

	@Override
	public int getCount() {
		return 7;
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
		RemoteViews views = new RemoteViews(myContext.getPackageName(), R.layout.day_name_layout);
		switch(position) {
			case 0: views.setTextViewText(R.id.textDayName, shortDays[firstDayOfWeekName == Calendar.SUNDAY ? Calendar.SUNDAY : Calendar.MONDAY]); break;
			case 1: views.setTextViewText(R.id.textDayName, shortDays[firstDayOfWeekName == Calendar.SUNDAY ? Calendar.MONDAY : Calendar.TUESDAY]); break;
			case 2: views.setTextViewText(R.id.textDayName, shortDays[firstDayOfWeekName == Calendar.SUNDAY ? Calendar.TUESDAY : Calendar.WEDNESDAY]); break;
			case 3: views.setTextViewText(R.id.textDayName, shortDays[firstDayOfWeekName == Calendar.SUNDAY ? Calendar.WEDNESDAY : Calendar.THURSDAY]); break;
			case 4: views.setTextViewText(R.id.textDayName, shortDays[firstDayOfWeekName == Calendar.SUNDAY ? Calendar.THURSDAY : Calendar.FRIDAY]); break;
			case 5: views.setTextViewText(R.id.textDayName, shortDays[firstDayOfWeekName == Calendar.SUNDAY ? Calendar.FRIDAY : Calendar.SATURDAY]); break;
			case 6: views.setTextViewText(R.id.textDayName, shortDays[firstDayOfWeekName == Calendar.SUNDAY ? Calendar.SATURDAY : Calendar.SUNDAY]); break;
			default: break;
		}
		views.setInt(R.id.layoutDayName, "setBackgroundColor", myContext.getResources().getColor(R.color.dayNameWeekCellBackground));
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
	}

	@Override
	public void onDataSetChanged() {
	}

	@Override
	public void onDestroy() {
	}
}
