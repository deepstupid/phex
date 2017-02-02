package phex.prefs.api;

import phex.common.log.NLogger;
import phex.util.ClassUtils;
import phex.util.StringUtils;

import java.io.Serializable;
import java.util.*;

public class PreferencesCodec
{
    private static final String SET_SER_POSTFIX = "[S:%d]";
    private static final String SET_DESER_POSTFIX = "[S:";
    private static final String LIST_SER_POSTFIX = "[L:%d]";
    private static final String LIST_DESER_POSTFIX = "[L:";
    
    public static List<String> deserializeList( String name, Preferences preferences )
    {
        List<String> list = new ArrayList<String>();
        List<String> names = preferences.getPrefixedPropertyNames( name + LIST_DESER_POSTFIX );
        Collections.sort( names, new ListPostfixKeyComparator() );
        for ( String key : names )
        {
            String value = preferences.getLoadedProperty( key );
            if ( !StringUtils.isEmpty( value ) )
            {
                list.add( value );
            }
        }
        return list;
    }
    
    public static Set<String> deserializeSet( String name, Preferences preferences )
    {
        Set<String> set = new HashSet<String>();
        String prefix = name + SET_DESER_POSTFIX;
        
        List<String> names = preferences.getPrefixedPropertyNames( prefix );
        for ( String key : names )
        {
            String value = preferences.getLoadedProperty( key );
            if ( !StringUtils.isEmpty( value ) )
            {
                set.add( value );
            }
        }
        return set;
    }

    /**
     * This method takes a Setting and serializes it into the given properties.
     * It takes care of using multiple elements for List and other special types. 
     * @param setting
     * @param properties
     */
    public static void serializeSetting( Setting<?> setting, Properties properties )
    {
        if ( setting == null )
        {
            throw new NullPointerException( "setting should not be null" );
        }
        String name = setting.getName();
        Object value = setting.get();
        if ( value instanceof String )
        {
            properties.setProperty( name, (String)value );
        }
        else if ( value instanceof Number )
        {
            properties.setProperty( name, ((Number)value).toString() );
        }
        else if ( value instanceof Boolean )
        {
            properties.setProperty( name, ((Boolean)value).toString() );
        }
        else if ( value instanceof Set )
        {
            Set<String> setValue = (Set<String>)value;
            int pos = 1;
            for( String elem : setValue )
            {
                properties.setProperty( name + String.format( SET_SER_POSTFIX, Integer.valueOf( pos++ ) ), elem );
            }
        }
        else if ( value instanceof List )
        {
            List<String> listValue = (List<String>)value;
            int listSize = listValue.size();
            for ( int i=0; i < listSize; i++ )
            {
                properties.setProperty( name + String.format( LIST_SER_POSTFIX, Integer.valueOf( i ) ), 
                    listValue.get( i ) );
            }
        }
        else
        {
            NLogger.error( PreferencesFactory.class, 
                "Unknwon settings value type: " + name + " / " + ClassUtils.getClassString( value ) );
        }
    }
    
    private static final class ListPostfixKeyComparator 
        implements Comparator<String>, Serializable
    {
        public int compare( String key1, String key2 )
        {
            if ( key1.equals( key2 ) )
            {
                return 0;
            }
            int idx1 = key1.lastIndexOf( LIST_DESER_POSTFIX ) + LIST_DESER_POSTFIX.length();
            int idx1E = key1.indexOf( ']', idx1 );
            String val1Str = key1.substring( idx1, idx1E );
            int val1;
            try
            {
                val1 = Integer.parseInt( val1Str );
            }
            catch ( NumberFormatException exp )
            {
                val1 = Integer.MAX_VALUE;
            }
            
            int idx2 = key2.lastIndexOf( LIST_DESER_POSTFIX ) + LIST_DESER_POSTFIX.length();
            int idx2E = key2.indexOf( ']', idx2 );
            String val2Str = key1.substring( idx2, idx2E );
            int val2;
            try
            {
                val2 = Integer.parseInt( val2Str );
            }
            catch ( NumberFormatException exp )
            {
                val2 = Integer.MAX_VALUE;
            }
            if ( val1 == val2 )
            {
                return key1.hashCode() - key2.hashCode();
            }
            return val1 - val2;
        }
        
    }
}
