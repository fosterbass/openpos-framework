import { NgModule } from '@angular/core';
import { NewRelicSink } from './new-relic-sink.service';

@NgModule({})
export class NewRelicSinkModule {
    constructor(newRelic: NewRelicSink) {}
}
