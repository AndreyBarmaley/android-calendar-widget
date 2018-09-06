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

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.widget.RemoteViews;



public class MyCalendarProvider extends AppWidgetProvider {
	final static String ACTION_GOTO_PREV = "my.apps.calendar.show_prev_calendar";
	final static String ACTION_GOTO_NEXT = "my.apps.calendar.show_next_calendar";
	final static String ACTION_GOTO_CURRENT = "my.apps.calendar.show_current_calendar";
	final static String ACTION_CLICK_CELL = "my.apps.calendar.click_cell";
	
	private Locale myLocale;
	private static int calendarOffset = 0;

	public MyCalendarProvider() {
		myLocale = Locale.getDefault(); // new Locale("ru","RU");
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onUpdate (Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int widgetId : appWidgetIds) {
			updateWidget(context, Calendar.getInstance(myLocale), myLocale, true, appWidgetManager, widgetId);
		}
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	    
		removeGotoCurrentAlarm(context);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		//Log.i("MyCalendar", "onReceive: " +  intent.getAction() + ", " + String.valueOf(widgetId));
	    
		if(intent.getAction().equalsIgnoreCase(ACTION_CLICK_CELL)) {
	    		int year = intent.getIntExtra("eventYear", 0);
	    		int month = intent.getIntExtra("eventMonth", 0);
	    		int day = intent.getIntExtra("eventDay", 0);

	    		if(year > 0 && month > 0 && day > 0) {
	    			Calendar cal = Calendar.getInstance(myLocale);
	    			// create event
	    			Intent intentEvent = new Intent(Intent.ACTION_INSERT);
	    			intentEvent.setData(Events.CONTENT_URI);
	    			intentEvent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, new GregorianCalendar(year, month, day, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE)).getTimeInMillis());
	    	    		intentEvent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, new GregorianCalendar(year, month, day, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE) + 5).getTimeInMillis());
	    	    		intentEvent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
	    			intentEvent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			context.startActivity(intentEvent);
	    			// update widget
	    			createGotoCurrentAlarm(context, widgetId, 20);
	    		}
		} else if(widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
        		boolean updateCalendar = false;
        	
    			if(intent.getAction().equalsIgnoreCase(ACTION_GOTO_NEXT)) {
    	    			calendarOffset += 1;
    	    			updateCalendar = true;
    	    			removeGotoCurrentAlarm(context);
    	    			createGotoCurrentAlarm(context, widgetId, 10);
    			} else if(intent.getAction().equalsIgnoreCase(ACTION_GOTO_PREV)) {
    	    			calendarOffset -= 1;
    	    			updateCalendar = true;
    	    			removeGotoCurrentAlarm(context);
    	    			createGotoCurrentAlarm(context, widgetId, 10);
    			} else if(intent.getAction().equalsIgnoreCase(ACTION_GOTO_CURRENT)) {
    	    			calendarOffset = 0;
    	    			updateCalendar = true;
    	    			removeGotoCurrentAlarm(context);
    			}

    			if(updateCalendar) {
    	    			Calendar cal = Calendar.getInstance(myLocale);
	    			cal.add(Calendar.MONTH, calendarOffset);

	    			updateWidget(context, cal, myLocale, calendarOffset == 0, AppWidgetManager.getInstance(context), widgetId);	        	
    			}
        	}
	}

	private void createGotoCurrentAlarm(Context context, int widgetId, int sec) {
	    	Intent gotoCurrentIntent = new Intent(context, MyCalendarProvider.class);
	        gotoCurrentIntent.setAction(ACTION_GOTO_CURRENT);
	        gotoCurrentIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
   		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + sec * 1000,
   							PendingIntent.getBroadcast(context, widgetId, gotoCurrentIntent, 0));
   		//Log.i("MyCalendar", "create alarm");
	}

	private void removeGotoCurrentAlarm(Context context) {
		Intent gotoCurrentIntent = new Intent(context, MyCalendarProvider.class);
	        gotoCurrentIntent.setAction(ACTION_GOTO_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(PendingIntent.getBroadcast(context, 0, gotoCurrentIntent, 0));
		//Log.i("MyCalendar", "remove alarm");
	}
	
	static private String stringFirstUpperCase(String str, Locale locale) {
		if(str == null)
			return str;
		String lower = str.toLowerCase(locale);
		return Character.toString(lower.charAt(0)).toUpperCase(locale).concat(lower.substring(1));
	}

	static private int firstDayOfMonth(Calendar cal) {
		int day = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int res = cal.get(Calendar.DAY_OF_WEEK);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return res;
	}

	static private void updateWidget(Context context, Calendar cal, Locale locale, boolean showCurrentDay, AppWidgetManager appWidgetManager, int widgetId) {
		DateFormatSymbols symbols = new DateFormatSymbols(locale);
		String months[] = symbols.getMonths();
		String shortDays[] = symbols.getShortWeekdays();

		String year = String.valueOf(cal.get(Calendar.YEAR));
		String monthName = stringFirstUpperCase(months[cal.get(Calendar.MONTH)], locale);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		views.setTextViewText(R.id.textMonthYear, monthName + " " + year);
		views.setInt(R.id.layoutWidget, "setBackgroundColor", context.getResources().getColor(R.color.widgetBackground));
		
		// update day names grid
 		Intent adapterDayNames = new Intent(context, MyDayNamesService.class);
		adapterDayNames.putExtra("shortDays", shortDays);
 		adapterDayNames.putExtra("firstDayOfWeekName", cal.getFirstDayOfWeek());
		adapterDayNames.setData(Uri.parse(adapterDayNames.toUri(Intent.URI_INTENT_SCHEME)));
		views.setRemoteAdapter(R.id.gridDayNames, adapterDayNames);

		// update day month grid
		Intent adapterDayMonth = new Intent(context, MyDayMonthService.class);
 		adapterDayMonth.putExtra("firstDayOfWeekName", cal.getFirstDayOfWeek());
 		adapterDayMonth.putExtra("firstDayOfMonthName", firstDayOfMonth(cal));
 		adapterDayMonth.putExtra("currentDay", showCurrentDay ? cal.get(Calendar.DAY_OF_MONTH) : 0);
 		adapterDayMonth.putExtra("currentMonth", cal.get(Calendar.MONTH));
 		adapterDayMonth.putExtra("currentYear", cal.get(Calendar.YEAR));
 		adapterDayMonth.putExtra("daysOfMonth", cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		adapterDayMonth.setData(Uri.parse(adapterDayMonth.toUri(Intent.URI_INTENT_SCHEME)));
		views.setRemoteAdapter(R.id.gridDayMonth, adapterDayMonth);

		// click prev
		Intent gotoPrevIntent = new Intent(context, MyCalendarProvider.class);
		gotoPrevIntent.setAction(ACTION_GOTO_PREV);
		gotoPrevIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		gotoPrevIntent.setData(Uri.parse(adapterDayMonth.toUri(Intent.URI_INTENT_SCHEME)));
		views.setOnClickPendingIntent(R.id.imagePrevMonth, PendingIntent.getBroadcast(context, widgetId, gotoPrevIntent, 0));

		// click next
		Intent gotoNextIntent = new Intent(context, MyCalendarProvider.class);
		gotoNextIntent.setAction(ACTION_GOTO_NEXT);
		gotoNextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		gotoNextIntent.setData(Uri.parse(adapterDayMonth.toUri(Intent.URI_INTENT_SCHEME)));
		views.setOnClickPendingIntent(R.id.imageNextMonth, PendingIntent.getBroadcast(context, widgetId, gotoNextIntent, 0));

		// click current
		Intent gotoCurrentIntent = new Intent(context, MyCalendarProvider.class);
		gotoCurrentIntent.setAction(ACTION_GOTO_CURRENT);
		gotoCurrentIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		gotoCurrentIntent.setData(Uri.parse(adapterDayMonth.toUri(Intent.URI_INTENT_SCHEME)));
		views.setOnClickPendingIntent(R.id.textMonthYear, PendingIntent.getBroadcast(context, widgetId, gotoCurrentIntent, 0));

		// click cell
		Intent clickCellIntent = new Intent(context, MyCalendarProvider.class);
		clickCellIntent.setAction(ACTION_CLICK_CELL);
		clickCellIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		clickCellIntent.setData(Uri.parse(adapterDayMonth.toUri(Intent.URI_INTENT_SCHEME)));
		views.setPendingIntentTemplate(R.id.gridDayMonth, PendingIntent.getBroadcast(context, 0, clickCellIntent, 0));

		appWidgetManager.updateAppWidget(widgetId, views);
		appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.gridDayMonth);
	}
}
