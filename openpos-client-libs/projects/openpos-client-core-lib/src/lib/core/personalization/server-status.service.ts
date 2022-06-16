import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, timer } from 'rxjs';
import { delay, distinct, map, retryWhen, switchMap, takeWhile } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class ServerStatusService {
    constructor(private http: HttpClient) {}

    observeInitializationStatus(baseUrl: string): Observable<ServerInitStatus> {
        while (baseUrl.endsWith('/')) {
            baseUrl = baseUrl.substring(0, baseUrl.length - 1);
        }

        return timer(0, 5000).pipe(
            switchMap(() => this.http.get<ServerInitData>(baseUrl + '/status/init').pipe(
                retryWhen(errors => errors.pipe(delay(5000)))
            )),
            distinct(),
            map(data => new ServerInitStatus(!!data.providers
                ?  data.providers.map(p => new ServerInitStatusProvider(p.name, p.currentState, p.message))
                : [])
            ),
            takeWhile(s => !s.isReady, true),
        );
    }
}

interface ServerInitData {
    readonly providers: ServerInitProviderData[];
}

interface ServerInitProviderData {
    readonly name: string;
    readonly currentState: string;
    readonly message: string;
}

export class ServerInitStatus {
    get isReady() {
        return !this.providers
            || this.providers.length === 0
            || this.providers.every((value) => value.isReady);
    }

    constructor(
        public readonly providers: ServerInitStatusProvider[]
    ) {}
}

export class ServerInitStatusProvider {
    get isReady() {
        return this.currentState === 'READY';
    }

    constructor(
        public readonly name: string,
        public readonly currentState: string,
        public readonly message: string
    ) {}
}
