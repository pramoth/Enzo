/*
 * Copyright (c) 2013 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.gauge;

import com.sun.javafx.css.converters.PaintConverter;
import eu.hansolo.enzo.gauge.skin.GaugeSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * Created by
 * User: hansolo
 * Date: 01.04.13
 * Time: 17:10
 */
public class Gauge extends Control {
    public static enum NeedleType {
        STANDARD("needle-standard");

        public final String STYLE_CLASS;

        private NeedleType(final String STYLE_CLASS) {
            this.STYLE_CLASS = STYLE_CLASS;
        }
    }
    public static enum TickLabelOrientation {
        ORTHOGONAL,
        HORIZONTAL,
        TANGENT
    }
    public static enum NumberFormat {
        AUTO("0"),
        STANDARD("0"),
        FRACTIONAL("0.0#"),
        SCIENTIFIC("0.##E0"),
        PERCENTAGE("##0.0%");

        private final DecimalFormat DF;

        private NumberFormat(final String FORMAT_STRING) {
            Locale.setDefault(new Locale("en", "US"));

            DF = new DecimalFormat(FORMAT_STRING);
        }

        public String format(final Number NUMBER) {
            return DF.format(NUMBER);
        }
    }

    public static final String       STYLE_CLASS_NEEDLE_STANDARD = NeedleType.STANDARD.STYLE_CLASS;

    // Default section colors
    private static final Color       DEFAULT_SECTION_0_FILL      = Color.rgb(0, 0, 178, 0.5);
    private static final Color       DEFAULT_SECTION_1_FILL      = Color.rgb(0, 128, 255, 0.5);
    private static final Color       DEFAULT_SECTION_2_FILL      = Color.rgb(  0, 255, 255, 0.5);
    private static final Color       DEFAULT_SECTION_3_FILL      = Color.rgb(  0, 255,  64, 0.5);
    private static final Color       DEFAULT_SECTION_4_FILL      = Color.rgb(128, 255,   0, 0.5);
    private static final Color       DEFAULT_SECTION_5_FILL      = Color.rgb(255, 255,   0, 0.5);
    private static final Color       DEFAULT_SECTION_6_FILL      = Color.rgb(255, 191,   0, 0.5);
    private static final Color       DEFAULT_SECTION_7_FILL      = Color.rgb(255, 128,   0, 0.5);
    private static final Color       DEFAULT_SECTION_8_FILL      = Color.rgb(255,  64,   0, 0.5);
    private static final Color       DEFAULT_SECTION_9_FILL      = Color.rgb(255,   0,   0, 0.5);
    private static final Color       DEFAULT_HISTOGRAM_FILL      = Color.rgb(  0, 200,   0, 0.3);

    // Default marker colors
    private static final Color       DEFAULT_MARKER_0_FILL       = Color.rgb(  0, 200,   0, 0.5);
    private static final Color       DEFAULT_MARKER_1_FILL       = Color.rgb(200, 200,   0, 0.5);
    private static final Color       DEFAULT_MARKER_2_FILL       = Color.rgb(200,   0,   0, 0.5);
    private static final Color       DEFAULT_MARKER_3_FILL       = Color.rgb(  0,   0, 200, 0.5);
    private static final Color       DEFAULT_MARKER_4_FILL       = Color.rgb(  0, 200, 200, 0.5);

    // CSS Pseudo classes
    private static final PseudoClass INTERACTIVE_PSEUDO_CLASS    = PseudoClass.getPseudoClass("interactive");

    private BooleanProperty                      interactive;

    private double                               _value;
    private DoubleProperty                       value;
    private double                               _oldValue;
    private double                               _minValue;
    private DoubleProperty                       minValue;
    private double                               _maxValue;
    private DoubleProperty                       maxValue;
    private double                               _threshold;
    private DoubleProperty                       threshold;
    private boolean                              _thresholdVisible;
    private BooleanProperty                      thresholdVisible;
    private double                               _minMeasuredValue;
    private DoubleProperty                       minMeasuredValue;
    private boolean                              _minMeasuredValueVisible;
    private BooleanProperty                      minMeasuredValueVisible;
    private double                               _maxMeasuredValue;
    private DoubleProperty                       maxMeasuredValue;
    private boolean                              _maxMeasuredValueVisible;
    private BooleanProperty                      maxMeasuredValueVisible;
    private int                                  _decimals;
    private IntegerProperty                      decimals;
    private String                               _title;
    private StringProperty                       title;
    private String                               _unit;
    private StringProperty                       unit;
    private boolean                              _animated;
    private BooleanProperty                      animated;
    private double                               animationDuration;
    private double                               _startAngle;
    private DoubleProperty                       startAngle;
    private double                               _angleRange;
    private DoubleProperty                       angleRange;
    private boolean                              _clockwise;
    private BooleanProperty                      clockwise;
    private Gauge.NeedleType                     _needleType;
    private ObjectProperty<NeedleType>           needleType;
    private Color                                _needleColor;
    private ObjectProperty<Color>                needleColor;
    private Gauge.TickLabelOrientation           _tickLabelOrientation;
    private ObjectProperty<TickLabelOrientation> tickLabelOrientation;
    private Gauge.NumberFormat                   _numberFormat;
    private ObjectProperty<NumberFormat>         numberFormat;
    private ObservableList<Section>              sections;
    private boolean                              _sectionsVisible;
    private BooleanProperty                      sectionsVisible;
    private ObservableMap<Marker, Rotate>        markers;
    private boolean                              _markersVisible;
    private BooleanProperty                      markersVisible;
    private double                               _majorTickSpace;
    private DoubleProperty                       majorTickSpace;
    private double                               _minorTickSpace;
    private DoubleProperty                       minorTickSpace;
    private boolean                              _plainValue;
    private BooleanProperty                      plainValue;
    private boolean                              _histogramEnabled;
    private BooleanProperty                      histogramEnabled;
    private boolean                              _dropShadowEnabled;
    private BooleanProperty                      dropShadowEnabled;

    // CSS styleable properties
    private ObjectProperty<Paint>                tickMarkFill;
    private ObjectProperty<Paint>                tickLabelFill;
    private ObjectProperty<Paint>                section0Fill;
    private ObjectProperty<Paint>                section1Fill;
    private ObjectProperty<Paint>                section2Fill;
    private ObjectProperty<Paint>                section3Fill;
    private ObjectProperty<Paint>                section4Fill;
    private ObjectProperty<Paint>                section5Fill;
    private ObjectProperty<Paint>                section6Fill;
    private ObjectProperty<Paint>                section7Fill;
    private ObjectProperty<Paint>                section8Fill;
    private ObjectProperty<Paint>                section9Fill;
    private ObjectProperty<Paint>                histogramFill;
    private ObjectProperty<Paint>                marker0Fill;
    private ObjectProperty<Paint>                marker1Fill;
    private ObjectProperty<Paint>                marker2Fill;
    private ObjectProperty<Paint>                marker3Fill;
    private ObjectProperty<Paint>                marker4Fill;


    // ******************** Constructors **************************************
    public Gauge() {
        getStyleClass().add("gauge");
        _value                   = 0;
        _oldValue                = 0;
        _minValue                = 0;
        _maxValue                = 100;
        _threshold               = 50;
        _thresholdVisible        = false;
        _minMeasuredValue        = 100;
        _minMeasuredValueVisible = false;
        _maxMeasuredValue        = 0;
        _maxMeasuredValueVisible = false;
        _decimals                = 2;
        _title                   = "title";
        _unit                    = "unit";
        _animated                = true;
        _startAngle              = 320;
        _angleRange              = 280;
        _clockwise               = true;
        _needleType              = NeedleType.STANDARD;
        _needleColor             = Color.RED;
        _tickLabelOrientation    = TickLabelOrientation.HORIZONTAL;
        _numberFormat            = NumberFormat.STANDARD;
        sections                 = FXCollections.observableArrayList();
        _sectionsVisible         = true;
        markers                  = FXCollections.observableHashMap();
        _markersVisible          = true;
        _majorTickSpace          = 10;
        _minorTickSpace          = 1;
        animationDuration        = 800;
        _plainValue              = true;
        _histogramEnabled        = false;
        _dropShadowEnabled       = true;
    }


    // ******************** Methods *******************************************
    public final double getValue() {
        return null == value ? _value : value.get();
    }
    public final void setValue(final double VALUE) {
        if (isInteractive()) return;
        if (null == value) {
            _oldValue = _value;
            _value = clamp(_minValue, _maxValue, VALUE);
        } else {
            _oldValue = value.get();
            value.set(clamp(getMinValue(), getMaxValue(), VALUE));
        }
    }
    public final ReadOnlyDoubleProperty valueProperty() {
        if (null == value) {
            value = new SimpleDoubleProperty(this, "value", _value);
        }
        return value;
    }

    public final double getOldValue() {
        return _oldValue;
    }

    public final double getMinValue() {
        return null == minValue ? _minValue : minValue.get();
    }
    public final void setMinValue(final double MIN_VALUE) {
        if (null == minValue) {
            _minValue = clamp(Double.MIN_VALUE, _maxValue, MIN_VALUE);
        } else {
            minValue.set(clamp(Double.MIN_VALUE, getMaxValue(), MIN_VALUE));
        }
        validate();
    }
    public final DoubleProperty minValueProperty() {
        if (null == minValue) {
            minValue = new SimpleDoubleProperty(this, "minValue", _minValue);
        }
        return minValue;
    }

    public final double getMaxValue() {
        return null == maxValue ? _maxValue : maxValue.get();
    }
    public final void setMaxValue(final double MAX_VALUE) {
        if (null == maxValue) {
            _maxValue = clamp(getMinValue(), Double.MAX_VALUE, MAX_VALUE);
        } else {
            maxValue.set(clamp(getMinValue(), Double.MAX_VALUE, MAX_VALUE));
        }
        validate();
    }
    public final DoubleProperty maxValueProperty() {
        if (null == maxValue) {
            maxValue = new SimpleDoubleProperty(this, "maxValue", _maxValue);
        }
        return maxValue;
    }

    public final double getThreshold() {
        return null == threshold ? _threshold : threshold.get();
    }
    public final void setThreshold(final double THRESHOLD) {
        if (null == threshold) {
            _threshold = clamp(getMinValue(), getMaxValue(), THRESHOLD);
        } else {
            threshold.set(clamp(getMinValue(), getMaxValue(), THRESHOLD));
        }
    }
    public final DoubleProperty thresholdProperty() {
        if (null == threshold) {
            threshold = new SimpleDoubleProperty(this, "threshold", _threshold);
        }
        return threshold;
    }

    public final double getMinMeasuredValue() {
        return null == minMeasuredValue ? _minMeasuredValue : minMeasuredValue.get();
    }
    public final void setMinMeasuredValue(final double MIN_MEASURED_VALUE) {
        if (null == minMeasuredValue) {
            _minMeasuredValue = MIN_MEASURED_VALUE;
        } else {
            minMeasuredValue.set(MIN_MEASURED_VALUE);
        }
    }
    public final ReadOnlyDoubleProperty minMeasuredValueProperty() {
        if (null == minMeasuredValue) {
            minMeasuredValue = new SimpleDoubleProperty(this, "minMeasuredValue", _minMeasuredValue);
        }
        return minMeasuredValue;
    }

    public final double getMaxMeasuredValue() {
        return null == maxMeasuredValue ? _maxMeasuredValue : maxMeasuredValue.get();
    }
    public final void setMaxMeasuredValue(final double MAX_MEASURED_VALUE) {
        if (null == maxMeasuredValue) {
            _maxMeasuredValue = MAX_MEASURED_VALUE;
        } else {
            maxMeasuredValue.set(MAX_MEASURED_VALUE);
        }
    }
    public final ReadOnlyDoubleProperty maxMeasuredValueProperty() {
        if (null == maxMeasuredValue) {
            maxMeasuredValue = new SimpleDoubleProperty(this, "maxMeasuredValue", _maxMeasuredValue);
        }
        return maxMeasuredValue;
    }

    public void resetMinMeasuredValue() {
        setMinMeasuredValue(_value);
    }
    public void resetMaxMeasuredValue() {
        setMaxMeasuredValue(_value);
    }
    public void resetMinAndMaxMeasuredValue() {
        setMinMeasuredValue(_value);
        setMaxMeasuredValue(_value);
    }

    public final int getDecimals() {
        return null == decimals ? _decimals : decimals.get();
    }
    public final void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = clamp(0, 3, DECIMALS);
        } else {
            decimals.set(clamp(0, 3, DECIMALS));
        }
    }
    public final IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new SimpleIntegerProperty(this, "decimals", _decimals);
        }
        return decimals;
    }

    public final String getTitle() {
        return null == title ? _title : title.get();
    }
    public final void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
        } else {
            title.set(TITLE);
        }
    }
    public final StringProperty titleProperty() {
        if (null == title) {
            title = new SimpleStringProperty(this, "title", _title);
        }
        return title;
    }

    public final String getUnit() {
        return null == unit ? _unit : unit.get();
    }
    public final void setUnit(final String UNIT) {
        if (null == unit) {
            _unit = UNIT;
        } else {
            unit.set(UNIT);
        }
    }
    public final StringProperty unitProperty() {
        if (null == unit) {
            unit = new SimpleStringProperty(this, "unit", _unit);
        }
        return unit;
    }

    public final boolean isAnimated() {
        return null == animated ? _animated : animated.get();
    }
    public final void setAnimated(final boolean ANIMATED) {
        if (null == animated) {
            _animated = ANIMATED;
        } else {
            animated.set(ANIMATED);
        }
    }
    public final BooleanProperty animatedProperty() {
        if (null == animated) {
            animated = new SimpleBooleanProperty(this, "animated", _animated);
        }
        return animated;
    }

    public double getStartAngle() {
        return null == startAngle ? _startAngle : startAngle.get();
    }
    public final void setStartAngle(final double START_ANGLE) {
        if (null == startAngle) {
            _startAngle = clamp(0, 360, START_ANGLE);
        } else {
            startAngle.set(clamp(0, 360, START_ANGLE));
        }
    }
    public final DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new SimpleDoubleProperty(this, "startAngle", _startAngle);
        }
        return startAngle;
    }

    public final double getAnimationDuration() {
        return animationDuration;
    }
    public final void setAnimationDuration(final double ANIMATION_DURATION) {
        animationDuration = clamp(20, 5000, ANIMATION_DURATION);
    }

    public final double getAngleRange() {
        return null == angleRange ? _angleRange : angleRange.get();
    }
    public final void setAngleRange(final double ANGLE_RANGE) {
        if (null == angleRange) {
            _angleRange = clamp(0.0, 360.0, ANGLE_RANGE);
        } else {
            angleRange.set(clamp(0.0, 360.0, ANGLE_RANGE));
        }
    }
    public final DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new SimpleDoubleProperty(this, "angleRange", _angleRange);
        }
        return angleRange;
    }

    public final boolean isClockwise() {
        return null == clockwise ? _clockwise : clockwise.get();
    }
    public final void setClockwise(final boolean CLOCKWISE) {
        if (null == clockwise) {
            _clockwise = CLOCKWISE;
        } else {
            clockwise.set(CLOCKWISE);
        }
    }
    public final BooleanProperty clockwiseProperty() {
        if (null == clockwise) {
            clockwise = new SimpleBooleanProperty(this, "clockwise", _clockwise);
        }
        return clockwise;
    }


    // Properties related to visualization
    public final Gauge.NeedleType getNeedleType() {
        return null == needleType ? _needleType : needleType.get();
    }
    public final void setNeedleType(final Gauge.NeedleType NEEDLE_TYPE) {
        if (null == needleType) {
            _needleType = NEEDLE_TYPE;
        } else {
            needleType.set(NEEDLE_TYPE);
        }
    }
    public final ObjectProperty<NeedleType> needleTypeProperty() {
        if (null == needleType) {
            needleType = new SimpleObjectProperty<>(this, "needleType", _needleType);
        }
        return needleType;
    }

    public final Color getNeedleColor() {
        return null == needleColor ? _needleColor : needleColor.get();
    }
    public final void setNeedleColor(final Color NEEDLE_COLOR) {
        if (null == needleColor) {
            _needleColor = NEEDLE_COLOR;
        } else {
            needleColor.set(NEEDLE_COLOR);
        }
    }
    public final ObjectProperty<Color> needleColorProperty() {
        if (null == needleColor) {
            needleColor = new SimpleObjectProperty<>(this, "needleColor", _needleColor);
        }
        return needleColor;
    }

    public final Gauge.TickLabelOrientation getTickLabelOrientation() {
        return null == tickLabelOrientation ? _tickLabelOrientation : tickLabelOrientation.get();
    }
    public final void setTickLabelOrientation(final Gauge.TickLabelOrientation TICK_LABEL_ORIENTATION) {
        if (null == tickLabelOrientation) {
            _tickLabelOrientation = TICK_LABEL_ORIENTATION;
        } else {
            tickLabelOrientation.set(TICK_LABEL_ORIENTATION);
        }
    }
    public final ObjectProperty<TickLabelOrientation> tickLabelOrientationProperty() {
        if (null == tickLabelOrientation) {
            tickLabelOrientation = new SimpleObjectProperty<>(this, "tickLabelOrientation", _tickLabelOrientation);
        }
        return tickLabelOrientation;
    }

    public final Gauge.NumberFormat getNumberFormat() {
        return null == numberFormat ? _numberFormat : numberFormat.get();
    }
    public final void setNumberFormat(final Gauge.NumberFormat NUMBER_FORMAT) {
        if (null == numberFormat) {
            _numberFormat = NUMBER_FORMAT;
        } else {
            numberFormat.set(NUMBER_FORMAT);
        }
    }
    public final ObjectProperty<NumberFormat> numberFormatProperty() {
        if (null == numberFormat) {
            numberFormat = new SimpleObjectProperty<>(this, "numberFormat", _numberFormat);
        }
        return numberFormat;
    }

    public final ObservableList<Section> getSections() {
        return sections;
    }
    public final void setSections(final List<Section> SECTIONS) {
        sections.setAll(SECTIONS);
    }
    public final void setSections(final Section... SECTIONS) {
        setSections(Arrays.asList(SECTIONS));
    }
    public final void addSection(final Section SECTION) {
        if (!sections.contains(SECTION)) sections.add(SECTION);
    }
    public final void removeSection(final Section SECTION) {
        if (sections.contains(SECTION)) sections.remove(SECTION);
    }

    public final ObservableMap<Marker, Rotate> getMarkers() {
        return markers;
    }
    public final void setMarkers(final List<Marker> MARKERS) {
        int markerCounter = 0;
        for (Marker marker : MARKERS) {
            Rotate markerRotate = new Rotate(180 - getStartAngle());
            marker.getTransforms().setAll(markerRotate);
            marker.getStyleClass().add("marker" + markerCounter);
            markers.put(marker, markerRotate);
            markerCounter++;
        }
    }
    public final void setMarkers(final Marker... MARKERS) {
        setMarkers(Arrays.asList(MARKERS));
    }
    public final void addMarker(final Marker MARKER) {
        if (!markers.keySet().contains(MARKER)) {
            Rotate markerRotate = new Rotate(180 - getStartAngle());
            MARKER.getTransforms().setAll(markerRotate);
            MARKER.getStyleClass().add("marker" + markers.size());
            markers.put(MARKER, markerRotate);
        }
    }
    public final void removeMarker(final Marker MARKER) {
        if (markers.keySet().contains(MARKER)) markers.remove(MARKER);
    }

    public final double getMajorTickSpace() {
        return null == majorTickSpace ? _majorTickSpace : majorTickSpace.get();
    }
    public final void setMajorTickSpace(final double MAJOR_TICK_SPACE) {
        if (null == majorTickSpace) {
            _majorTickSpace = MAJOR_TICK_SPACE;
        } else {
            majorTickSpace.set(MAJOR_TICK_SPACE);
        }
    }
    public final DoubleProperty majorTickSpaceProperty() {
        if (null == majorTickSpace) {
            majorTickSpace = new SimpleDoubleProperty(this, "majorTickSpace", _majorTickSpace);
        }
        return majorTickSpace;
    }

    public final double getMinorTickSpace() {
        return null == minorTickSpace ? _minorTickSpace : minorTickSpace.get();
    }
    public final void setMinorTickSpace(final double MINOR_TICK_SPACE) {
        if (null == minorTickSpace) {
            _minorTickSpace = MINOR_TICK_SPACE;
        } else {
            minorTickSpace.set(MINOR_TICK_SPACE);
        }
    }
    public final DoubleProperty minorTickSpaceProperty() {
        if (null == minorTickSpace) {
            minorTickSpace = new SimpleDoubleProperty(this, "minorTickSpace", _minorTickSpace);
        }
        return minorTickSpace;
    }

    /**
     * @return true if the value of the gauge will be drawn without a blend effect
     */
    public final boolean isPlainValue() {
        return null == plainValue ? _plainValue : plainValue.get();
    }

    /**
     * If set to true the value will be visualized without a blend effect
     * @param PLAIN_VALUE
     */
    public final void setPlainValue(final boolean PLAIN_VALUE) {
        if (null == plainValue) {
            _plainValue = PLAIN_VALUE;
        } else {
            plainValue.set(PLAIN_VALUE);
        }
    }
    public final BooleanProperty plainValueProperty() {
        if (null == plainValue) {
            plainValue = new SimpleBooleanProperty(this, "plainValue", _plainValue);
        }
        return plainValue;
    }

    public final boolean isHistogramEnabled() {
        return null == histogramEnabled ? _histogramEnabled : histogramEnabled.get();
    }
    public final void setHistogramEnabled(final boolean HISTOGRAM_ENABLED) {
        if (null == histogramEnabled) {
            _histogramEnabled = HISTOGRAM_ENABLED;
        } else {
            histogramEnabled.set(HISTOGRAM_ENABLED);
        }
    }
    public final BooleanProperty histogramEnabledProperty() {
        if (null == histogramEnabled) {
            histogramEnabled = new SimpleBooleanProperty(this, "histogramEnabled", _histogramEnabled);
        }
        return histogramEnabled;
    }

    public final boolean isDropShadowEnabled() {
        return null == dropShadowEnabled ? _dropShadowEnabled : dropShadowEnabled.get();
    }
    public final void setDropShadowEnabled(final boolean DROP_SHADOW_ENABLED) {
        if (null == dropShadowEnabled) {
            _dropShadowEnabled = DROP_SHADOW_ENABLED;
        } else {
            dropShadowEnabled.set(DROP_SHADOW_ENABLED);
        }
    }
    public final BooleanProperty dropShadowEnabledProperty() {
        if (null == dropShadowEnabled) {
            dropShadowEnabled = new SimpleBooleanProperty(this, "dropShadowEnabled", _dropShadowEnabled);
        }
        return dropShadowEnabled;
    }

    public final boolean isSectionsVisible() {
        return null == sectionsVisible ? _sectionsVisible : sectionsVisible.get();
    }
    public final void setSectionsVisible(final boolean SECTIONS_VISIBLE) {
        if (null == sectionsVisible) {
            _sectionsVisible = SECTIONS_VISIBLE;
        } else {
            sectionsVisible.set(SECTIONS_VISIBLE);
        }
    }
    public final BooleanProperty sectionsVisibleProperty() {
        if (null == sectionsVisible) {
            sectionsVisible = new SimpleBooleanProperty(this, "sectionsVisible", _sectionsVisible);
        }
        return sectionsVisible;
    }

    public final boolean isMarkersVisible() {
        return null == markersVisible ? _markersVisible : markersVisible.get();
    }
    public final void setMarkersVisible(final boolean MARKERS_VISIBLE) {
        if (null == markersVisible) {
            _markersVisible = MARKERS_VISIBLE;
        } else {
            markersVisible.set(MARKERS_VISIBLE);
        }
    }
    public final BooleanProperty markersVisibleProperty() {
        if (null == markersVisible) {
            markersVisible = new SimpleBooleanProperty(this, "markersVisible", _markersVisible);
        }
        return markersVisible;
    }

    public final boolean isThresholdVisible() {
        return null == thresholdVisible ? _thresholdVisible : thresholdVisible.get();
    }
    public final void setThresholdVisible(final boolean THRESHOLD_VISIBLE) {
        if (null == thresholdVisible) {
            _thresholdVisible = THRESHOLD_VISIBLE;
        } else {
            thresholdVisible.set(THRESHOLD_VISIBLE);
        }
    }
    public final BooleanProperty thresholdVisibleProperty() {
        if (null == thresholdVisible) {
            thresholdVisible = new SimpleBooleanProperty(this, "thresholdVisible", _thresholdVisible);
        }
        return thresholdVisible;
    }

    public final boolean isMinMeasuredValueVisible() {
        return null == minMeasuredValueVisible ? _minMeasuredValueVisible : minMeasuredValueVisible.get();
    }
    public final void setMinMeasuredValueVisible(final boolean MIN_MEASURED_VALUE_VISIBLE) {
        if (null == minMeasuredValueVisible) {
            _minMeasuredValueVisible = MIN_MEASURED_VALUE_VISIBLE;
        } else {
            minMeasuredValueVisible.set(MIN_MEASURED_VALUE_VISIBLE);
        }
    }
    public final BooleanProperty minMeasuredValueVisibleProperty() {
        if (null == minMeasuredValueVisible) {
            minMeasuredValueVisible = new SimpleBooleanProperty(this, "minMeasuredValueVisible", _minMeasuredValueVisible);
        }
        return minMeasuredValueVisible;
    }

    public final boolean isMaxMeasuredValueVisible() {
        return null == maxMeasuredValueVisible ? _maxMeasuredValueVisible : maxMeasuredValueVisible.get();
    }
    public final void setMaxMeasuredValueVisible(final boolean MAX_MEASURED_VALUE_VISIBLE) {
        if (null == maxMeasuredValueVisible) {
            _maxMeasuredValueVisible = MAX_MEASURED_VALUE_VISIBLE;
        } else {
            maxMeasuredValueVisible.set(MAX_MEASURED_VALUE_VISIBLE);
        }
    }
    public final BooleanProperty maxMeasuredValueVisibleProperty() {
        if (null == maxMeasuredValueVisible) {
            maxMeasuredValueVisible = new SimpleBooleanProperty(this, "maxMeasuredValueVisible", _maxMeasuredValueVisible);
        }
        return maxMeasuredValueVisible;
    }

    private double clamp(final double MIN_VALUE, final double MAX_VALUE, final double VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }
    private int clamp(final int MIN_VALUE, final int MAX_VALUE, final int VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }
    private Duration clamp(final Duration MIN_VALUE, final Duration MAX_VALUE, final Duration VALUE) {
        if (VALUE.lessThan(MIN_VALUE)) return MIN_VALUE;
        if (VALUE.greaterThan(MAX_VALUE)) return MAX_VALUE;
        return VALUE;
    }

    private void validate() {
        if (getThreshold() < getMinValue()) setThreshold(getMinValue());
        if (getThreshold() > getMaxValue()) setThreshold(getMaxValue());
        if (getValue() < getMinValue()) setValue(getMinValue());
        if (getValue() > getMaxValue()) setValue(getMaxValue());
    }


    // ******************** CSS Stylable Properties ***************************
    public final Paint getTickMarkFill() {
        return null == tickMarkFill ? Color.BLACK : tickMarkFill.get();
    }
    public final void setTickMarkFill(Paint value) {
        tickMarkFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickMarkFillProperty() {
        if (null == tickMarkFill) {
            tickMarkFill = new StyleableObjectProperty<Paint>(Color.BLACK) {

                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_MARK_FILL; }

                @Override public Object getBean() { return Gauge.this; }

                @Override public String getName() { return "tickMarkFill"; }
            };
        }
        return tickMarkFill;
    }

    public final Paint getTickLabelFill() {
        return null == tickLabelFill ? Color.BLACK : tickLabelFill.get();
    }
    public final void setTickLabelFill(Paint value) {
        tickLabelFillProperty().set(value);
    }
    public final ObjectProperty<Paint> tickLabelFillProperty() {
        if (null == tickLabelFill) {
            tickLabelFill = new StyleableObjectProperty<Paint>(Color.BLACK) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.TICK_LABEL_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickLabelFill"; }
            };
        }
        return tickLabelFill;
    }

    public final Paint getSection0Fill() {
        return null == section0Fill ? DEFAULT_SECTION_0_FILL : section0Fill.get();
    }
    public final void setSection0Fill(Paint value) {
        section0FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section0FillProperty() {
        if (null == section0Fill) {
            section0Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_0_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_0_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section0Fill"; }
            };
        }
        return section0Fill;
    }

    public final Paint getSection1Fill() {
        return null == section1Fill ? DEFAULT_SECTION_1_FILL : section1Fill.get();
    }
    public final void setSection1Fill(Paint value) {
        section1FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section1FillProperty() {
        if (null == section1Fill) {
            section1Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_1_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_1_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section1Fill"; }
            };
        }
        return section1Fill;
    }

    public final Paint getSection2Fill() {
        return null == section2Fill ? DEFAULT_SECTION_2_FILL : section2Fill.get();
    }
    public final void setSection2Fill(Paint value) {
        section2FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section2FillProperty() {
        if (null == section2Fill) {
            section2Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_2_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_2_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section2Fill"; }
            };
        }
        return section2Fill;
    }

    public final Paint getSection3Fill() {
        return null == section3Fill ? DEFAULT_SECTION_3_FILL : section3Fill.get();
    }
    public final void setSection3Fill(Paint value) {
        section3FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section3FillProperty() {
        if (null == section3Fill) {
            section3Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_3_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_3_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section3Fill"; }
            };
        }
        return section3Fill;
    }

    public final Paint getSection4Fill() {
        return null == section4Fill ? DEFAULT_SECTION_4_FILL : section4Fill.get();
    }
    public final void setSection4Fill(Paint value) {
        section4FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section4FillProperty() {
        if (null == section4Fill) {
            section4Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_4_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_4_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section4Fill"; }
            };
        }
        return section4Fill;
    }

    public final Paint getSection5Fill() {
        return null == section5Fill ? DEFAULT_SECTION_5_FILL : section5Fill.get();
    }
    public final void setSection5Fill(Paint value) {
        section5FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section5FillProperty() {
        if (null == section5Fill) {
            section5Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_5_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_5_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section5Fill"; }
            };
        }
        return section5Fill;
    }

    public final Paint getSection6Fill() {
        return null == section6Fill ? DEFAULT_SECTION_6_FILL : section6Fill.get();
    }
    public final void setSection6Fill(Paint value) {
        section6FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section6FillProperty() {
        if (null == section6Fill) {
            section6Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_6_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_6_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section6Fill"; }
            };
        }
        return section6Fill;
    }

    public final Paint getSection7Fill() {
        return null == section7Fill ? DEFAULT_SECTION_7_FILL : section7Fill.get();
    }
    public final void setSection7Fill(Paint value) {
        section7FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section7FillProperty() {
        if (null == section7Fill) {
            section7Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_7_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_7_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section7Fill"; }
            };
        }
        return section7Fill;
    }

    public final Paint getSection8Fill() {
        return null == section8Fill ? DEFAULT_SECTION_8_FILL : section8Fill.get();
    }
    public final void setSection8Fill(Paint value) {
        section8FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section8FillProperty() {
        if (null == section8Fill) {
            section8Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_8_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_8_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section8Fill"; }
            };
        }
        return section8Fill;
    }

    public final Paint getSection9Fill() {
        return null == section9Fill ? DEFAULT_SECTION_9_FILL : section9Fill.get();
    }
    public final void setSection9Fill(Paint value) {
        section9FillProperty().set(value);
    }
    public final ObjectProperty<Paint> section9FillProperty() {
        if (null == section9Fill) {
            section9Fill = new StyleableObjectProperty<Paint>(DEFAULT_SECTION_9_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.SECTION_9_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "section9Fill"; }
            };
        }
        return section9Fill;
    }

    public final Paint getHistogramFill() {
        return null == histogramFill ? DEFAULT_HISTOGRAM_FILL : histogramFill.get();
    }
    public final void setHistogramFill(Paint value) {
        histogramFillProperty().set(value);
    }
    public final ObjectProperty<Paint> histogramFillProperty() {
        if (null == histogramFill) {
            histogramFill = new StyleableObjectProperty<Paint>(DEFAULT_HISTOGRAM_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.HISTOGRAM_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "histogramFill"; }
            };
        }
        return section9Fill;
    }

    public final Paint getMarker0Fill() {
        return null == marker0Fill ? DEFAULT_MARKER_0_FILL : marker0Fill.get();
    }
    public final void setMarker0Fill(Paint value) {
        marker0FillProperty().set(value);
    }
    public final ObjectProperty<Paint> marker0FillProperty() {
        if (null == marker0Fill) {
            marker0Fill = new StyleableObjectProperty<Paint>(DEFAULT_MARKER_0_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.MARKER_0_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "marker0Fill"; }
            };
        }
        return section0Fill;
    }

    public final Paint getMarker1Fill() {
        return null == marker1Fill ? DEFAULT_MARKER_1_FILL : marker1Fill.get();
    }
    public final void setMarker1Fill(Paint value) {
        marker1FillProperty().set(value);
    }
    public final ObjectProperty<Paint> marker1FillProperty() {
        if (null == marker1Fill) {
            marker1Fill = new StyleableObjectProperty<Paint>(DEFAULT_MARKER_1_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.MARKER_1_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "marker1Fill"; }
            };
        }
        return marker1Fill;
    }

    public final Paint getMarker2Fill() {
        return null == marker2Fill ? DEFAULT_MARKER_2_FILL : marker2Fill.get();
    }
    public final void setMarker2Fill(Paint value) {
        marker2FillProperty().set(value);
    }
    public final ObjectProperty<Paint> marker2FillProperty() {
        if (null == marker2Fill) {
            marker2Fill = new StyleableObjectProperty<Paint>(DEFAULT_MARKER_2_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.MARKER_2_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "marker2Fill"; }
            };
        }
        return marker2Fill;
    }

    public final Paint getMarker3Fill() {
        return null == marker3Fill ? DEFAULT_MARKER_3_FILL : marker3Fill.get();
    }
    public final void setMarker3Fill(Paint value) {
        marker3FillProperty().set(value);
    }
    public final ObjectProperty<Paint> marker3FillProperty() {
        if (null == marker3Fill) {
            marker3Fill = new StyleableObjectProperty<Paint>(DEFAULT_MARKER_3_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.MARKER_3_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "marker3Fill"; }
            };
        }
        return marker3Fill;
    }

    public final Paint getMarker4Fill() {
        return null == marker4Fill ? DEFAULT_MARKER_4_FILL : marker4Fill.get();
    }
    public final void setMarker4Fill(Paint value) {
        marker4FillProperty().set(value);
    }
    public final ObjectProperty<Paint> marker4FillProperty() {
        if (null == marker4Fill) {
            marker4Fill = new StyleableObjectProperty<Paint>(DEFAULT_MARKER_4_FILL) {
                @Override public CssMetaData getCssMetaData() { return StyleableProperties.MARKER_4_FILL; }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "marker4Fill"; }
            };
        }
        return marker4Fill;
    }


    // ******************** CSS Pseudo Classes ********************************
    public final boolean isInteractive() {
        return null == interactive ? false : interactive.get();
    }
    public final void setInteractive(final boolean INTERACTIVE) {
        interactiveProperty().set(INTERACTIVE);
    }
    public final BooleanProperty interactiveProperty() {
        if (null == interactive) {
            interactive = new BooleanPropertyBase(false) {
                @Override protected void invalidated() { pseudoClassStateChanged(INTERACTIVE_PSEUDO_CLASS, get()); }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "interactive"; }
            };
        }
        return interactive;
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        return new GaugeSkin(this);
    }

    @Override protected String getUserAgentStylesheet() {
        return getClass().getResource("gauge.css").toExternalForm();
    }
    
    private static class StyleableProperties {
        private static final CssMetaData<Gauge, Paint> TICK_MARK_FILL =
            new CssMetaData<Gauge, Paint>("-tick-mark-fill", PaintConverter.getInstance(), Color.BLACK) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.tickMarkFill || !gauge.tickMarkFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.tickMarkFillProperty();
                }
            };

        private static final CssMetaData<Gauge, Paint> TICK_LABEL_FILL =
            new CssMetaData<Gauge, Paint>("-tick-label-fill", PaintConverter.getInstance(), Color.BLACK) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.tickLabelFill || !gauge.tickLabelFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.tickLabelFillProperty();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_0_FILL =
            new CssMetaData<Gauge, Paint>("-section0-fill", PaintConverter.getInstance(), DEFAULT_SECTION_0_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section0Fill || !gauge.section0Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section0FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection0Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_1_FILL =
            new CssMetaData<Gauge, Paint>("-section1-fill", PaintConverter.getInstance(), DEFAULT_SECTION_1_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section1Fill || !gauge.section1Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section1FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection1Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_2_FILL =
            new CssMetaData<Gauge, Paint>("-section2-fill", PaintConverter.getInstance(), DEFAULT_SECTION_2_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section2Fill || !gauge.section2Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section2FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection2Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_3_FILL =
            new CssMetaData<Gauge, Paint>("-section3-fill", PaintConverter.getInstance(), DEFAULT_SECTION_3_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section3Fill || !gauge.section3Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section3FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection3Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_4_FILL =
            new CssMetaData<Gauge, Paint>("-section4-fill", PaintConverter.getInstance(), DEFAULT_SECTION_4_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section4Fill || !gauge.section4Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section4FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection4Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_5_FILL =
            new CssMetaData<Gauge, Paint>("-section5-fill", PaintConverter.getInstance(), DEFAULT_SECTION_5_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section5Fill || !gauge.section5Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section5FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection5Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_6_FILL =
            new CssMetaData<Gauge, Paint>("-section6-fill", PaintConverter.getInstance(), DEFAULT_SECTION_6_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section6Fill || !gauge.section6Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section6FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection6Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_7_FILL =
            new CssMetaData<Gauge, Paint>("-section7-fill", PaintConverter.getInstance(), DEFAULT_SECTION_7_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section7Fill || !gauge.section7Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section7FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection7Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_8_FILL =
            new CssMetaData<Gauge, Paint>("-section8-fill", PaintConverter.getInstance(), DEFAULT_SECTION_8_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section8Fill || !gauge.section8Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section8FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection8Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> SECTION_9_FILL =
            new CssMetaData<Gauge, Paint>("-section9-fill", PaintConverter.getInstance(), DEFAULT_SECTION_9_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.section9Fill || !gauge.section9Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.section9FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getSection9Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> HISTOGRAM_FILL =
            new CssMetaData<Gauge, Paint>("-histogram-fill", PaintConverter.getInstance(), DEFAULT_HISTOGRAM_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.histogramFill || !gauge.histogramFill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.histogramFillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getHistogramFill();
                }
            };

        private static final CssMetaData<Gauge, Paint> MARKER_0_FILL =
            new CssMetaData<Gauge, Paint>("-marker0-fill", PaintConverter.getInstance(), DEFAULT_MARKER_0_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.marker0Fill || !gauge.marker0Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.marker0FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getMarker0Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> MARKER_1_FILL =
            new CssMetaData<Gauge, Paint>("-marker1-fill", PaintConverter.getInstance(), DEFAULT_MARKER_1_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.marker1Fill || !gauge.marker1Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.marker1FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getMarker1Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> MARKER_2_FILL =
            new CssMetaData<Gauge, Paint>("-marker2-fill", PaintConverter.getInstance(), DEFAULT_MARKER_2_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.marker2Fill || !gauge.marker2Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.marker2FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getMarker2Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> MARKER_3_FILL =
            new CssMetaData<Gauge, Paint>("-marker3-fill", PaintConverter.getInstance(), DEFAULT_MARKER_3_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.marker3Fill || !gauge.marker3Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.marker3FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getMarker3Fill();
                }
            };

        private static final CssMetaData<Gauge, Paint> MARKER_4_FILL =
            new CssMetaData<Gauge, Paint>("-marker4-fill", PaintConverter.getInstance(), DEFAULT_MARKER_4_FILL) {

                @Override public boolean isSettable(Gauge gauge) {
                    return null == gauge.marker4Fill || !gauge.marker4Fill.isBound();
                }

                @Override public StyleableProperty<Paint> getStyleableProperty(Gauge gauge) {
                    return (StyleableProperty) gauge.marker4FillProperty();
                }

                @Override public Paint getInitialValue(Gauge gauge) {
                    return gauge.getMarker4Fill();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                               TICK_MARK_FILL,
                               TICK_LABEL_FILL,
                               SECTION_0_FILL,
                               SECTION_1_FILL,
                               SECTION_2_FILL,
                               SECTION_3_FILL,
                               SECTION_4_FILL,
                               SECTION_5_FILL,
                               SECTION_6_FILL,
                               SECTION_7_FILL,
                               SECTION_8_FILL,
                               SECTION_9_FILL,
                               HISTOGRAM_FILL,
                               MARKER_0_FILL,
                               MARKER_1_FILL,
                               MARKER_2_FILL,
                               MARKER_3_FILL,
                               MARKER_4_FILL
            );
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }


    // ******************** Event handling ************************************
    public final ObjectProperty<EventHandler<GaugeEvent>> onThresholdExceededProperty() { return onThresholdExceeded; }
    public final void setOnThresholdExceeded(EventHandler<GaugeEvent> value) { onThresholdExceededProperty().set(value); }
    public final EventHandler<GaugeEvent> getOnThresholdExceeded() { return onThresholdExceededProperty().get(); }
    private ObjectProperty<EventHandler<GaugeEvent>> onThresholdExceeded = new ObjectPropertyBase<EventHandler<GaugeEvent>>() {
        @Override public Object getBean() { return this; }
        @Override public String getName() { return "onThresholdExceeded";}
    };

    public final ObjectProperty<EventHandler<GaugeEvent>> onThresholdUnderrunProperty() { return onThresholdUnderrun; }
    public final void setOnThresholdUnderrun(EventHandler<GaugeEvent> value) { onThresholdUnderrunProperty().set(value); }
    public final EventHandler<GaugeEvent> getOnThresholdUnderrun() { return onThresholdUnderrunProperty().get(); }
    private ObjectProperty<EventHandler<GaugeEvent>> onThresholdUnderrun = new ObjectPropertyBase<EventHandler<GaugeEvent>>() {
        @Override public Object getBean() { return this; }
        @Override public String getName() { return "onThresholdUnderrun";}
    };

    public void fireGaugeEvent(final GaugeEvent EVENT) {
        final EventHandler<GaugeEvent> HANDLER;
        final EventType TYPE = EVENT.getEventType();
        if (GaugeEvent.THRESHOLD_EXCEEDED == TYPE) {
            HANDLER = getOnThresholdExceeded();
        } else if (GaugeEvent.THRESHOLD_UNDERRUN == TYPE) {
            HANDLER = getOnThresholdUnderrun();
        } else {
            HANDLER = null;
        }

        if (null == HANDLER) return;

        HANDLER.handle(EVENT);
    }
}
