import { Injectable, Injector } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EMPTY, Observable, throwError } from 'rxjs';

import {
  all,
  oneOf,
  optional,
  StartupTask,
  StartupTaskCombinator
} from './startup-task';

@Injectable()
export class TestService {
  counter = 0;

  doAction() {
    this.counter++;
  }
}

@Injectable({
  providedIn: 'root'
})
export class SequentiallyMarkedStartupTask implements StartupTask {
  readonly markers: string[] = [];

  execute(): void {
    this.markers.push(
      'execute' + this.markers.length
    );
  }
}

@Injectable({
  providedIn: 'root'
})
export class ExceptionThrowingStartupTask implements StartupTask {
  readonly markers: string[] = [];

  execute(): void {
    throw new Error('Test');
  }
}

@Injectable({
  providedIn: 'root'
})
export class WithDependenciesStartupTask implements StartupTask {
  constructor(private _testService: TestService) { }

  execute() {
    this._testService.doAction();
  }
}

@Injectable({
  providedIn: 'root'
})
export class PromiseSuccessStartupTask implements StartupTask {
  execute(): Promise<void> {
    return Promise.resolve();
  }
}

@Injectable({
  providedIn: 'root'
})
export class ObservableSuccessStartupTask implements StartupTask {
  execute(): Observable<void> {
    return EMPTY;
  }
}

@Injectable({
  providedIn: 'root'
})
export class PromiseErrorStartupTask implements StartupTask {
  execute(): Promise<void> {
    return Promise.reject(new Error('Testing'));
  }
}

@Injectable({
  providedIn: 'root'
})
export class ObservableErrorStartupTask implements StartupTask {
  execute(): Observable<void> {
    return throwError(() => new Error('Testing'));
  }
}

describe('StartupTask', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        TestService
      ]
    }).compileComponents();
  });

  describe('all', () => {
    it('should inject services', async () => {
      const sequence = all(
        WithDependenciesStartupTask
      );

      await run(sequence);

      const testService = TestBed.inject(TestService);
      expect(testService.counter).toBe(1);
    });

    it('should execute sequentially', async () => {
      const sequence = all(
        SequentiallyMarkedStartupTask,
        SequentiallyMarkedStartupTask,
        SequentiallyMarkedStartupTask
      );

      await run(sequence);

      const task = TestBed.inject(SequentiallyMarkedStartupTask);
      expect(task.markers.length).toBe(3);
      expect(task.markers[0]).toBe('execute0');
      expect(task.markers[1]).toBe('execute1');
      expect(task.markers[2]).toBe('execute2');
    });

    it('should fail on any synchronous error', async () => {
      const sequence = all(
        SequentiallyMarkedStartupTask,
        ExceptionThrowingStartupTask
      );

      await expectAsync(run(sequence)).toBeRejected();
    });

    it('should fail on any promise error', async () => {
      const sequence = all(
        SequentiallyMarkedStartupTask,
        PromiseErrorStartupTask
      );

      await expectAsync(run(sequence)).toBeRejected();
    });

    it('should fail on any observable error', async () => {
      const sequence = all(
        SequentiallyMarkedStartupTask,
        ObservableErrorStartupTask
      );

      await expectAsync(run(sequence)).toBeRejected();
    });
  });

  describe('oneof', () => {
    it('should inject services', async () => {
      const sequence = oneOf(
        WithDependenciesStartupTask
      );

      await run(sequence);

      const testService = TestBed.inject(TestService);
      expect(testService.counter).toBe(1);
    });

    it('should stop after first success', async () => {
      const sequence = oneOf(
        SequentiallyMarkedStartupTask,
        SequentiallyMarkedStartupTask,
        SequentiallyMarkedStartupTask
      );

      await run(sequence);

      const task = TestBed.inject(SequentiallyMarkedStartupTask);
      expect(task.markers.length).toBe(1);
      expect(task.markers[0]).toBe('execute0');
    });

    it('should continue after synchronous failure', async () => {
      const sequence = oneOf(
        ExceptionThrowingStartupTask,
        ExceptionThrowingStartupTask,
        SequentiallyMarkedStartupTask
      );

      await run(sequence);

      const task = TestBed.inject(SequentiallyMarkedStartupTask);
      expect(task.markers.length).toBe(1);
      expect(task.markers[0]).toBe('execute0');
    });

    it('should continue after promise failure', async () => {
      const sequence = oneOf(
        PromiseErrorStartupTask,
        PromiseErrorStartupTask,
        SequentiallyMarkedStartupTask
      );

      await run(sequence);

      const task = TestBed.inject(SequentiallyMarkedStartupTask);
      expect(task.markers.length).toBe(1);
      expect(task.markers[0]).toBe('execute0');
    });

    it('should continue after observable failure', async () => {
      const sequence = oneOf(
        ObservableErrorStartupTask,
        ObservableErrorStartupTask,
        SequentiallyMarkedStartupTask
      );

      await run(sequence);

      const task = TestBed.inject(SequentiallyMarkedStartupTask);
      expect(task.markers.length).toBe(1);
      expect(task.markers[0]).toBe('execute0');
    });
  });

  describe('optional', () => {
    it('should inject services', async () => {
      const sequence = optional(
        WithDependenciesStartupTask
      );

      await run(sequence);

      const testService = TestBed.inject(TestService);
      expect(testService.counter).toBe(1);
    });

    it('should succeed on synchronous error', async () => {
      const sequence = optional(ExceptionThrowingStartupTask);

      await expectAsync(run(sequence)).not.toBeRejected();
    });

    it('should succeed on promise error', async () => {
      const sequence = optional(PromiseErrorStartupTask);

      await expectAsync(run(sequence)).not.toBeRejected();
    });

    it('should succeed on observable error', async () => {
      const sequence = optional(ObservableErrorStartupTask);

      await expectAsync(run(sequence)).not.toBeRejected();
    });

    it('should succeed on success', async () => {
      const sequence = optional(SequentiallyMarkedStartupTask);

      await expectAsync(run(sequence)).not.toBeRejected();

      const task = TestBed.inject(SequentiallyMarkedStartupTask);
      expect(task.markers.length).toBe(1);
      expect(task.markers[0]).toBe('execute0');
    });

    it('should succeed on promise success', async () => {
      const sequence = optional(PromiseSuccessStartupTask);

      await expectAsync(run(sequence)).not.toBeRejected();
    });

    it('should succeed on observable success', async () => {
      const sequence = optional(ObservableErrorStartupTask);

      await expectAsync(run(sequence)).not.toBeRejected();
    });
  });
});

async function run(task: StartupTaskCombinator): Promise<void> {
  const injector = TestBed.inject(Injector);
  await task.executeTask(injector);
}
