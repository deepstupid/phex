//package phex.common.log;
//
//import ch.qos.logback.classic.pattern.ClassicConverter;
//import ch.qos.logback.classic.spi.LoggingEvent;
//import phex.common.PhexVersion;
//
//public class PhexVersionConverter extends ClassicConverter
//{
//    public PhexVersionConverter()
//    {
//    }
//
//    @Override
//    public String convert(LoggingEvent arg0)
//    {
//        return PhexVersion.getBuild();
//    }
//}