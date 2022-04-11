import { Injectable } from '@angular/core';
import { iif, Observable, of, throwError } from 'rxjs';
import { Zeroconf, ZeroconfResult, ZeroconfService } from './zeroconf';

import { Service } from 'mdns';
import { ElectronPlatform } from '../platforms/electron.platform';
import { filter, switchMap } from 'rxjs/operators';

const ipv4Matcher = /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/;
const ipv6Matcher = /^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?\d)?\d)\.){3}(25[0-5]|(2[0-4]|1?\d)?\d)|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?\d)?\d)\.){3}(25[0-5]|(2[0-4]|1?\d)?\d))$/;

@Injectable()
export class MDnsZeroconf implements Zeroconf {
    constructor(private _electron: ElectronPlatform) { }

    private static _makeService(service: Service): ZeroconfService {
        return {
            name: service.name,
            domain: service.replyDomain,
            hostname: service.host,
            port: service.port,
            type: service.type.name,
            ipv4Addresses: service.addresses
                .filter(a => ipv4Matcher.test(a)),
            ipv6Addresses: service.addresses
                .filter(a => ipv6Matcher.test(a)),
            txtRecord: service.txtRecord
        };
    }

    isAvailable(): Observable<boolean> {
        return of(this._electron.platformPresent() && !!window.openposElectron?.zeroconf);
    }

    watch(type: string, domain: string): Observable<ZeroconfResult> {
        return this.isAvailable().pipe(
            filter(value => value),
            switchMap(() => new Observable<ZeroconfResult>(observer => {
                if (!window.openposElectron?.zeroconf) {
                    return;
                }

                const uplistner = window.openposElectron.zeroconf.addListener('service-up', type, domain, (service) => {
                    observer.next({
                        action: 'resolved',
                        service: MDnsZeroconf._makeService(service)
                    });
                });

                const downlistner = window.openposElectron.zeroconf.addListener('service-down', type, domain, (service) => {
                    observer.next({
                        action: 'removed',
                        service: MDnsZeroconf._makeService(service)
                    });
                });

                return async () => {
                    (await uplistner)();
                    (await downlistner)();
                };
            }))
        );
    }

    deviceName(): Observable<string> {
        return this.isAvailable().pipe(
            switchMap(value => iif(
                () => value,

                of(window.openposElectron.os.hostname),

                throwError(new Error('not running in electron platform'))
            ))
        );
    }
}
