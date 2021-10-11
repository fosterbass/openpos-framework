import { Observable } from 'rxjs';

export interface IPrinter {
    name(): string;

    isSupported(): boolean;

    print(html: string): Observable<void>;
}
