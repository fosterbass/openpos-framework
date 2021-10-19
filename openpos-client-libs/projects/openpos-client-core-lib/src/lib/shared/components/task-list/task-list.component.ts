import { Directive } from '@angular/core';
import { TaskListManagerService } from './task-list-manager.service';

@Directive({
  // tslint:disable-next-line: directive-selector
  selector: '[app-task-list]',
  providers: [TaskListManagerService]
})
export class TaskListDirective {

}
