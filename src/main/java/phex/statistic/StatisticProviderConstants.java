/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  --- SVN Information ---
 *  $Id: StatisticProviderConstants.java 4232 2008-07-16 21:07:23Z gregork $
 */
package phex.statistic;

public interface StatisticProviderConstants {
    String TOTAL_BANDWIDTH_PROVIDER = "TotalBandwidthProvider";
    String NETWORK_BANDWIDTH_PROVIDER = "NetworkBandwidthProvider";
    String DOWNLOAD_BANDWIDTH_PROVIDER = "DownloadBandwidthProvider";
    String UPLOAD_BANDWIDTH_PROVIDER = "UploadBandwidthProvider";

    String TOTALMSG_IN_PROVIDER = "TotalMsgInProvider";
    String PINGMSG_IN_PROVIDER = "PingMsgInProvider";
    String PONGMSG_IN_PROVIDER = "PongMsgInProvider";
    String PUSHMSG_IN_PROVIDER = "PushMsgInProvider";
    String QUERYMSG_IN_PROVIDER = "QueryMsgInProvider";
    String QUERYHITMSG_IN_PROVIDER = "QueryHitMsgInProvider";

    String TOTALMSG_OUT_PROVIDER = "TotalMsgOutProvider";
    String PINGMSG_OUT_PROVIDER = "PingMsgOutProvider";
    String PONGMSG_OUT_PROVIDER = "PongMsgOutProvider";
    String PUSHMSG_OUT_PROVIDER = "PushMsgOutProvider";
    String QUERYMSG_OUT_PROVIDER = "QueryMsgOutProvider";
    String QUERYHITMSG_OUT_PROVIDER = "QueryHitMsgOutProvider";

    String DROPEDMSG_TOTAL_PROVIDER = "DropedMsgTotalProvider";
    String DROPEDMSG_IN_PROVIDER = "DropedMsgInProvider";
    String DROPEDMSG_OUT_PROVIDER = "DropedMsgOutProvider";

    String UPTIME_PROVIDER = "UptimeProvider";
    String DAILY_UPTIME_PROVIDER = "DailyUptimeProvider";

    String SESSION_UPLOAD_COUNT_PROVIDER = "SessionUploadCountProvider";
    String TOTAL_UPLOAD_COUNT_PROVIDER = "TotalUploadCountProvider";
    String SESSION_DOWNLOAD_COUNT_PROVIDER = "SessionDownloadCountProvider";
    String TOTAL_DOWNLOAD_COUNT_PROVIDER = "TotalDownloadCountProvider";

    String PUSH_DOWNLOAD_ATTEMPTS_PROVIDER = "PushDownloadAttemptsProvider";
    String PUSH_DOWNLOAD_SUCESS_PROVIDER = "PushDownloadSucessProvider";
    String PUSH_DOWNLOAD_FAILURE_PROVIDER = "PushDownloadFailureProvider";

    String PUSH_DLDPUSHPROXY_ATTEMPTS_PROVIDER = "PushDldPushProxyAttemptsProvider";
    String PUSH_DLDPUSHPROXY_SUCESS_PROVIDER = "PushDldPushProxySuccessProvider";

    String PUSH_UPLOAD_ATTEMPTS_PROVIDER = "PushUploadAttemptsProvider";
    String PUSH_UPLOAD_SUCESS_PROVIDER = "PushUploadSucessProvider";
    String PUSH_UPLOAD_FAILURE_PROVIDER = "PushUploadFailureProvider";

    String HORIZON_HOST_COUNT_PROVIDER = "HorizonHostCountProvider";
    String HORIZON_FILE_COUNT_PROVIDER = "HorizonFileCountProvider";
    String HORIZON_FILE_SIZE_PROVIDER = "HorizonFileSizeProvider";


}