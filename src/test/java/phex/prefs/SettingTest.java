///*
// *  PHEX - The pure-java Gnutella-servent.
// *  Copyright (C) 2001 - 2011 Phex Development Group
// *
// *  This program is free software; you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation; either version 2 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program; if not, write to the Free Software
// *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// *
// *  --- SVN Information ---
// *  $Id: Setting.java 4046 2007-11-19 17:13:59Z gregork $
// */
//package phex.prefs;
//
//import org.bushe.swing.event.annotation.EventTopicSubscriber;
//import org.junit.Test;
//import phex.event.PhexEventService;
//import phex.event.PhexEventServiceImpl;
//import phex.event.PhexEventTopics;
//import phex.prefs.api.Preferences;
//import phex.prefs.api.PreferencesFactory;
//import phex.prefs.api.Setting;
//import phex.prefs.api.SettingChangedEvent;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static org.junit.Assert.assertEquals;
//
//public class SettingTest
//{
//    @Test
//    public void eventTest()
//    {
//        PhexEventService eventService = new PhexEventServiceImpl();
//
//        Preferences preferences = new Preferences( null, eventService );
//
//        final Set<String> settingSet = new HashSet<String>();
//        Setting<String> test1 = PreferencesFactory.createStringSetting("test1", "", preferences);
//        settingSet.add( test1.getName() );
//        Setting<Boolean> test2 = PreferencesFactory.createBoolSetting("test2", true, preferences);
//        settingSet.add( test2.getName() );
//        Setting<Float> test3 = PreferencesFactory.createFloatSetting("test3", 0F, preferences);
//        settingSet.add( test3.getName() );
//        Setting<Integer> test4 = PreferencesFactory.createIntRangeSetting("test4", 0, 0, 99, preferences);
//        settingSet.add( test4.getName() );
//        Setting<Integer> test5 = PreferencesFactory.createIntSetting("test5", 0, preferences);
//        settingSet.add( test5.getName() );
//        Setting<List<String>> test6 = PreferencesFactory.createListSetting("test6", preferences);
//        settingSet.add( test6.getName() );
//        Setting<Long> test7 = PreferencesFactory.createLongSetting("test7", 0L, preferences);
//        settingSet.add( test7.getName() );
//        Setting<Set<String>> test8 = PreferencesFactory.createSetSetting("test8", preferences);
//        settingSet.add( test8.getName() );
//        Setting<Short> test9 = PreferencesFactory.createShortRangeSetting("test9", (short)0, (short)0, (short)99, preferences);
//        settingSet.add( test9.getName() );
//
//        eventService.processAnnotations( new EventTestConsumer(settingSet));
//
//        test1.set("a");
//        test2.set(false);
//        test3.set(2F);
//        test4.set(2);
//        test5.set(2);
//        // collection require explicit change fire.
//        test6.fireChanged();
//        test7.set(1L);
//        // collection require explicit change fire.
//        test8.fireChanged();
//        test9.set((short)1);
//
//        assertEquals( 0, settingSet.size() );
//    }
//    public static final class EventTestConsumer
//    {
//        private final Set<String> settingSet;
//
//        private EventTestConsumer(Set<String> settingSet)
//        {
//            this.settingSet = settingSet;
//        }
//
//        //@EventTopicSubscriber(topic=PhexEventTopics.Prefs_Changed)
//        public void onPrefsChanged( String topic, SettingChangedEvent event )
//        {
//            settingSet.remove( event.getSource().getName() );
//        }
//    }
//}
