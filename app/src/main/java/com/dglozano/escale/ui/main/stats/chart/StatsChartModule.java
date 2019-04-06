package com.dglozano.escale.ui.main.stats.chart;

import com.github.mikephil.charting.formatter.IValueFormatter;

import java.text.DecimalFormat;

import dagger.Module;
import dagger.Provides;

@Module
public class StatsChartModule {

    @Provides
    IValueFormatter provideDecimalFormatChart() {
        return (value, entry, dataSetIndex, viewPortHandler) -> new DecimalFormat("###,###.#").format(value).replace(",", ".");
    }

}
