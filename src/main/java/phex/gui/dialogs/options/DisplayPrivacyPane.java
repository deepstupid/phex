
package phex.gui.dialogs.options;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import phex.gui.common.GUIRegistry;
import phex.gui.prefs.PrivacyPrefs;
import phex.utils.Localizer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DisplayPrivacyPane extends OptionsSettingsPane
{
    private GUIRegistry guiRegistry;
    private JCheckBox showKeywordSearchHistoryChkbx;
    private JCheckBox showBrowseHostHistoryChkbx;
    private JCheckBox showConnectToHistoryChkbx;
    private JCheckBox showLibrarySearchCountChkbx;
    private JCheckBox showLibraryUploadCountChkbx;
    private JCheckBox showSecurityRuleTriggerCountChkbx;
    private JLabel showContent;

    public DisplayPrivacyPane()
    {
        super( "PrivacySetting_Privacy" );
        guiRegistry = GUIRegistry.getInstance();
    }

    /**
     * Called when preparing this settings pane for display the first time. Can
     * be overriden to implement the look of the settings pane.
     */
    @Override
    protected void prepareComponent()
    {
        setLayout( new BorderLayout() );
        
        //JPanel contentPanel = new FormDebugPanel();
        JPanel contentPanel = new JPanel();
        add( contentPanel, BorderLayout.CENTER );
        
        FormLayout layout = new FormLayout(
            "10dlu, d, 6dlu, d, 2dlu:grow", // columns
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p , 3dlu, p" ); //  rows 
        layout.setRowGroups( new int[][]{{11, 5 }} );
        contentPanel.setLayout( layout );
        
        PanelBuilder builder = new PanelBuilder( layout, contentPanel );
        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator( Localizer.getString( "PrivacySettings_PrivacySettings" ), 
            cc.xywh( 1, 1, 5, 1 ) );

        showContent = new JLabel(Localizer.getString("PrivacySettings_ClearOptionChecklist"));
        builder.add( showContent, cc.xy( 2, 3 ));

        showKeywordSearchHistoryChkbx = new JCheckBox(
            Localizer.getString("PrivacySettings_KeywordSearchHistroy") ,
            PrivacyPrefs.ClearKeywordSearchHistoryDialog.get().booleanValue() );
        //showKeywordSearchHistory.setToolTipText( Localizer.getString( 
          //  "PromptSettings_KeywordSearchHistory" ) );
        builder.add( showKeywordSearchHistoryChkbx, cc.xy( 2, 5 ) );

        showBrowseHostHistoryChkbx = new JCheckBox(
            Localizer.getString("PrivacySettings_BrowseHostHistory") ,
            PrivacyPrefs.ClearBrowseHostHistoryDialog.get().booleanValue() );
        //showBrowseHostHistory.setToolTipText(
          //  Localizer.getString( "PromptSettings_BrowseHostHistory" ) );
        builder.add( showBrowseHostHistoryChkbx, cc.xy( 2, 7 ) );
        
        showConnectToHistoryChkbx = new JCheckBox(
            Localizer.getString("PrivacySettings_ConnectToHistory") ,
            PrivacyPrefs.ClearConnectToHistoryDialog.get().booleanValue());
        //showConnectToHistory.setToolTipText(
          //  Localizer.getString( "PromptSettings_ConnectToHistory" ) );
        builder.add( showConnectToHistoryChkbx, cc.xy( 2, 9 ) );

        showLibrarySearchCountChkbx = new JCheckBox(
            Localizer.getString( "PrivacySettings_LibrarySearchCount" ),
            PrivacyPrefs.ClearLibrarySearchCountDialog.get().booleanValue() );
        //showLibrarySearchCount.setToolTipText( Localizer.getString( 
          //  "PromptSettings_LibrarySearchCount" ) );
        builder.add( showLibrarySearchCountChkbx, cc.xy( 2, 11 ) );

        showLibraryUploadCountChkbx = new JCheckBox(
            Localizer.getString( "PrivacySettings_LibraryUploadCount" ),
            PrivacyPrefs.ClearLibraryUploadCountDialog.get().booleanValue() );
        //showLibraryUploadCount.setToolTipText(
          //  Localizer.getString( "PromptSettings_LibraryUploadCount" ) );
        builder.add( showLibraryUploadCountChkbx, cc.xy( 2, 13 ) );
        
        showSecurityRuleTriggerCountChkbx = new JCheckBox(
            Localizer.getString( "PrivacySettings_SecurityRuleTriggerCount" ),
            PrivacyPrefs.ClearSecurityRuleTriggerCountDialog.get().booleanValue() );
        //showSecurityRuleTriggerCout.setToolTipText(
         //   Localizer.getString( "PromptSettings_SecurityRuleTriggerCout" ) );
        builder.add( showSecurityRuleTriggerCountChkbx, cc.xy( 2, 15 ) );

    }

    /**
     * Override this method if you like to apply and save changes made on
     * settings pane. To trigger saving of the configuration if any value was
     * changed call triggerConfigSave().
     */
    @Override
    public void saveAndApplyChanges( HashMap inputDic )
    {
        boolean KeywordSearchHistoryOptions = showKeywordSearchHistoryChkbx.isSelected();
        PrivacyPrefs.ClearKeywordSearchHistoryDialog.set( Boolean.valueOf( KeywordSearchHistoryOptions ) );

        boolean BrowseHostHistoryOptions = showBrowseHostHistoryChkbx.isSelected();
        PrivacyPrefs.ClearBrowseHostHistoryDialog.set( Boolean.valueOf( BrowseHostHistoryOptions ) );

        boolean ConnectToHistoryOptions = showConnectToHistoryChkbx.isSelected();
        PrivacyPrefs.ClearConnectToHistoryDialog.set( Boolean.valueOf( ConnectToHistoryOptions ) );

        boolean LibrarySearchCountOptions = showLibrarySearchCountChkbx.isSelected();
        PrivacyPrefs.ClearLibrarySearchCountDialog.set( Boolean.valueOf( LibrarySearchCountOptions ) );

        boolean LibraryUploadCountOptions = showLibraryUploadCountChkbx.isSelected();
        PrivacyPrefs.ClearLibraryUploadCountDialog.set( Boolean.valueOf( LibraryUploadCountOptions ) );

        boolean SecurityRuleTriggerCountOptions = showSecurityRuleTriggerCountChkbx.isSelected();
        PrivacyPrefs.ClearSecurityRuleTriggerCountDialog.set( Boolean.valueOf( SecurityRuleTriggerCountOptions ) );
    }
}
