from common import *
from ontology_populator.implementations.core import *
from ontology_populator.implementations.knime import KnimeImplementation, KnimeParameter, KnimeBaseBundle

piechart_visualizer_implementation = KnimeImplementation(
    name = "Pie Chart Visualizer",
    algorithm = cb.PieChart,
    parameters = [

        KnimeParameter("generateImagemodel", XSD.boolean, False, 'generateImage', path="model"),
        KnimeParameter("category", XSD.string, "", "cat", path="model"), ### Very Important
        KnimeParameter("CI SettingsModelID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/cat_Internals"),
        KnimeParameter("CI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/cat_Internals"),
        KnimeParameter("aggr", XSD.string, "Sum", 'aggr', path="model"), ### Very Important [Occurrence Count, Sum, Average]
        KnimeParameter("AGGI SettingsModelID", XSD.string, "SMID_string", 'SettingsModelID',
                       path="model/aggr_Internals"),
        KnimeParameter("AGGI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/aggr_Internals"),
        KnimeParameter("reportOnMissingValues", XSD.boolean, True, 'reportOnMissingValues', path="model"),
        KnimeParameter("ROMVI SettingsModelID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/reportOnMissingValues_Internals"),
        KnimeParameter("ROMVI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/reportOnMissingValues_Internals"),
        KnimeParameter("includeMissValCat", XSD.boolean, True, 'includeMissValCat', path="model"), ### Very Important
        KnimeParameter("IMVCI SettingsModelID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/includeMissValCat_Internals"),
        KnimeParameter("IMVCI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/includeMissValCat_Internals"),
        KnimeParameter("freq", XSD.string, "frequency", 'freq', path="model"), ### Optional Very Important
        KnimeParameter("FrI SettingsModelID", XSD.string, "SMID_string", 'SettingsModelID',
                       path="model/freq_Internals"),
        KnimeParameter("FrI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/freq_Internals"),
        KnimeParameter("processInMemory", XSD.boolean, True, 'processInMemory', path="model"),
        KnimeParameter("PIMI SettingsModelID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/processInMemory_Internals"),
        KnimeParameter("PIMI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/processInMemory_Internals"),
        KnimeParameter("title", XSD.string, "Pie Chart", 'title', path="model"), ### Very Important
        KnimeParameter("TI SettingsModelID", XSD.string, "SMID_string", 'SettingsModelID',
                       path="model/title_Internals"),
        KnimeParameter("TI EnabledStatus", XSD.boolean, True, "EnabledStatus",
                      path="model/title_Internals"),
        KnimeParameter("subtitle", XSD.string, "", 'subtitle', path="model"), ### Very Important
        KnimeParameter("SUBI SettingsModelID", XSD.string, "SMID_string", 'SettingsModelID',
                       path="model/subtitle_Internals"),
        KnimeParameter("SUBI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/subtitle_Internals"),
        KnimeParameter("togglePie", XSD.boolean, False, 'togglePie', path="model"), ### Very Important [True-->Donut, False-->Pie]
        KnimeParameter("TPI SettingsModelID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/togglePie_Internals"),
        KnimeParameter("TPI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/togglePie_Internals"),
        KnimeParameter("holeSize", XSD.double, 0.35, 'holeSize', path="model"),
        KnimeParameter("HSI SettingsModelID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/holeSize_Internals"),
        KnimeParameter("HSI EnabledStatus", XSD.boolean, False, 'EnabledStatus',
                       path="model/holeSize_Internals"),
        KnimeParameter("insideTitle", XSD.string, "", 'insideTitle', path="model"),
        KnimeParameter("INI SettingsModelID", XSD.string, "SMID_string", 'SettingsModelID',
                       path="model/insideTitle_Internals"),
        KnimeParameter("INI EnabledStatus", XSD.boolean, False, 'EnabledStatus',
                       path="model/insideTitle_Internals"),
        KnimeParameter("customColors", XSD.boolean, False, 'customColors', path="model"), ### Optional Very Important
        KnimeParameter("CCI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/customColors_Internals"),
        KnimeParameter("CCI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/customColors_Internals"),
        KnimeParameter("legend", XSD.boolean, True, 'legend', path="model"), ### Very Important 
        KnimeParameter("LEI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/legend_Internals"),
        KnimeParameter("LEI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/legend_Internals"),
        KnimeParameter("showLabels", XSD.boolean, True, 'showLabels', path="model"), ### Very Important
        KnimeParameter("SLI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModelID',
                       path="model/showLabels_Internals"),
        KnimeParameter("SLI EnabledStatus", XSD.string, "SMID_boolean", 'EnabledStatus',
                       path="model/showLabels_Internals"),
        KnimeParameter("labelType", XSD.string, "Value", 'labelType', path="model"), ### Very Important
        KnimeParameter("LTI SettingsModeID", XSD.string, "SMID_string", 'SettingsModeID',
                       path="model/labelType_Internals"),
        KnimeParameter("LTI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/labelType_Internals"),
        KnimeParameter("labelThreshold", XSD.double, 0.05, 'labelThreshold', path="model"),
        KnimeParameter("LTHI SettingsModeID", XSD.string, "SMID_double", 'SettingsModeID',
                       path="model/labelThreshold_Internals"),
        KnimeParameter("LTHI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/labelThreshold_Internals"),
        KnimeParameter("displayFullScreenButton", XSD.boolean, True, 'displayFullScreenButton', path="model"),
        KnimeParameter("DFBI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/displayFullScreenButton_Internals"),
        KnimeParameter("DFBI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/displayFullScreenButton_Internals"),
        KnimeParameter("svg_width", XSD.integer, 600, 'width', path="model/svg"), ### Very Important
        KnimeParameter("svg_height", XSD.integer, 400, 'height', path="model/svg"), ### Very Important
        KnimeParameter("svg_fullscreen", XSD.boolean, True, 'fullscreen', path="model/svg"), ### Very Important
        KnimeParameter("svg_showFullscreen", XSD.boolean, True, 'showFullscreen', path="model/svg"), ### Very Important
        KnimeParameter("SVGI SettingsModeID", XSD.string, "SMID_svg", 'SettingsModeID',
                       path="model/svg_Internals"),
        KnimeParameter("SVGI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/svg_Internals"),
        KnimeParameter("showWarnings", XSD.boolean, True, 'showWarnings', path="model"),
        KnimeParameter("SWI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/showWarnings_Internals"),
        KnimeParameter("SWI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/showWarnings_Internals"),
        KnimeParameter("enableViewControls", XSD.boolean, True, 'enableViewControls', path="model"),
        KnimeParameter("EVCI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableViewControls_Internals"),
        KnimeParameter("EVCI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableViewControls_Internals"),
        KnimeParameter("enableTitleEdit", XSD.boolean, True, 'enableTitleEdit', path="model"),
        KnimeParameter("ETEI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableTitleEdit_Internals"),
        KnimeParameter("ETEI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableTitleEdit_Internals"),
        KnimeParameter("enableSubtitleEdit", XSD.boolean, True, 'enableSubtitleEdit', path="model"),
        KnimeParameter("ESEI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableSubtitleEdit_Internals"),
        KnimeParameter("ESEI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableSubtitleEdit_Internals"),
        KnimeParameter("enableDonutToggle", XSD.boolean, True, 'enableDonutToggle', path="model"),
        KnimeParameter("EDTI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableDonutToggle_Internals"),
        KnimeParameter("EDTI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableDonutToggle_Internals"),
        KnimeParameter("enableHoleEdit", XSD.boolean, True, 'enableHoleEdit', path="model"),
        KnimeParameter("EHEI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableHoleEdit_Internals"),
        KnimeParameter("EHEI EnabledStatus", XSD.boolean, "SMID_boolean", 'EnabledStatus',
                       path="model/enableHoleEdit_Internals"),
        KnimeParameter("enableInsideTitleEdit", XSD.boolean, True, 'enableInsideTitleEdit', path="model"),
        KnimeParameter("EIEI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableInsideTitleEdit_Internals"),
        KnimeParameter("EIEI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableInsideTitleEdit_Internals"),
        KnimeParameter("enableColumnChooser", XSD.boolean, True, 'enableColumnChooser', path="model"),
        KnimeParameter("ECCI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableColumnChooser_Internals"),
        KnimeParameter("ECCI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableColumnChooser_Internals"),
        KnimeParameter("enableLabelEdit", XSD.boolean, True, 'enableLabelEdit', path="model"),
        KnimeParameter("ELEI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableLabelEdit_Internals"),
        KnimeParameter("ELEI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableLabelEdit_Internals"),
        KnimeParameter("enableSwitchMissValCat", XSD.boolean, True, 'enableSwitchMissValCat', path="model"),
        KnimeParameter("ESMI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableSwitchMissValCat_Internals"),
        KnimeParameter("ESMI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableSwitchMissValCat_Internals"),
        KnimeParameter("enableSelection", XSD.boolean, True, 'enableSelection', path="model"),
        KnimeParameter("ESI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/enableSelection_Internals"),
        KnimeParameter("ESI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/enableSelection_Internals"),
        KnimeParameter("subscribeToSelection", XSD.boolean, True, 'subscribeToSelection', path="model"),
        KnimeParameter("STSI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/subscribeToSelection_Internals"),
        KnimeParameter("STSI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/subscribeToSelection_Internals"),
        KnimeParameter("publishSelection", XSD.boolean, True, 'publishSelection', path="model"),
        KnimeParameter("PSI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/publishSelection_Internals"),
        KnimeParameter("PSI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/publishSelection_Internals"),
        KnimeParameter("displayClearSelectionButton", XSD.boolean, True, 'displayClearSelectionButton', path="model"),
        KnimeParameter("DCSI SettingsModeID", XSD.string, "SMID_boolean", 'SettingsModeID',
                       path="model/displayClearSelectionButton_Internals"),
        KnimeParameter("DCSI EnabledStatus", XSD.boolean, True, 'EnabledStatus',
                       path="model/displayClearSelectionButton_Internals"),
        KnimeParameter("hideInWizard", XSD.boolean, False, 'hideInWizard', path="model"),
        KnimeParameter("maxRows", XSD.integer, 2500, 'maxRows', path="model"),
        KnimeParameter("generateImage", XSD.boolean, False, 'generateImage', path="model"),
        KnimeParameter("customCSS", XSD.string, "", 'customCSS', path="model")

    ],
    input = [
        [cb.TabularDataset, cb.NormalizedTabularDatasetShape, cb.NonNullTabularDatasetShape]
    ],
    output = [
        cb.PieChart
    ],
    implementation_type=tb.VisualizerImplementation,
    knime_node_factory='',
    knime_bundle=KnimeBaseBundle,
)

piechart_expparams = [
        list(piechart_visualizer_implementation.parameters.keys())[1], ### category column
        list(piechart_visualizer_implementation.parameters.keys())[4], ### aggregation type: [Occurrence Count, Sum, Average]
        list(piechart_visualizer_implementation.parameters.keys())[10], ### include missing value category: [True, False]
        list(piechart_visualizer_implementation.parameters.keys())[13], ### frequency column (optional)
        list(piechart_visualizer_implementation.parameters.keys())[19], ### pie chart visualization title
        list(piechart_visualizer_implementation.parameters.keys())[22], ### pie chart visualization subtitle
        list(piechart_visualizer_implementation.parameters.keys())[25], ### toggle Pie [True-->Donut, False-->Pie]
        list(piechart_visualizer_implementation.parameters.keys())[34], ### having custom colors for the plot(optional)
        list(piechart_visualizer_implementation.parameters.keys())[37], ### display legends for the visualization
        list(piechart_visualizer_implementation.parameters.keys())[40], ### display labels for the visualization
        list(piechart_visualizer_implementation.parameters.keys())[52], ### svg visualization width [default 600]
        list(piechart_visualizer_implementation.parameters.keys())[53], ### svg visualization height [default 400]
]

piechart_visualizer_component = Component(
    name = "Pie Chart Visualizer",
    implementation = piechart_visualizer_implementation,
    exposed_parameters = piechart_expparams,
    overriden_parameters = [
        ParameterSpecification(impl_para, impl_para.default_value) for impl_para in list(set(piechart_visualizer_implementation.parameters.keys()) - set(piechart_expparams))
    ],
    transformations = ''
)