import { NgModule } from '@angular/core';
import { CommerceServerSink } from './commerce-server-sink.service';

@NgModule({})
export class CommerceServerSinkModule {
    constructor(serverLogger: CommerceServerSink) { }
}
