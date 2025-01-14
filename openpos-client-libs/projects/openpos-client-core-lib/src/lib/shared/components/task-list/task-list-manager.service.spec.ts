import { cold, getTestScheduler } from 'jasmine-marbles';
import { TaskCheckAllBoxComponent } from '../task-check-all-box/task-check-all-box.component';
import { TaskCheckAllStateEnum } from '../task-check-all-box/task-check-all-state.enum';
import { TaskCheckBoxComponent } from '../task-check-box/task-check-box.component';
import { TaskListManagerService } from './task-list-manager.service';

describe('task-list-manager.service', () => {


    class MockTaskCheckBoxComponent extends TaskCheckBoxComponent {
        constructor() {
            super(null, null);
        }
    }

    class MockTaskCheckAllBoxComponent extends TaskCheckAllBoxComponent {
        constructor() {
            super(null, null);
        }
    }

    let checkAllBox: MockTaskCheckAllBoxComponent;
    let sut: TaskListManagerService;
    let checkBox1: MockTaskCheckBoxComponent;
    let checkBox2: MockTaskCheckBoxComponent;



    beforeEach(() => {
        sut = new TaskListManagerService();
        checkBox1 = new MockTaskCheckBoxComponent();
        checkBox2 = new MockTaskCheckBoxComponent();
        checkAllBox = new MockTaskCheckAllBoxComponent();
    });

    describe('registerTaskCheckBox', () => {
        it('should register checkbox and subscribe for changes', () => {

            const checked1Spy = spyOnProperty(checkBox1, 'checkedChange').and.returnValue(cold('--'));

            sut.registerTaskCheckBox(checkBox1);

            expect(checked1Spy).toHaveBeenCalled();

        });
    });

    describe('registerCheckAllBox', () => {
        it('should register check all box and subscribe for changes', () => {
            const stateChangedSpy = spyOnProperty(checkAllBox, 'stateChanged').and.returnValue(cold('--'));

            sut.registerCheckAllBox(checkAllBox);

            expect(stateChangedSpy).toHaveBeenCalled();
        });
    });


    it('check all should check all registered check boxes', () => {

        spyOnProperty(checkBox1, 'checkedChange').and.returnValue(cold('--'));
        spyOnProperty(checkBox2, 'checkedChange').and.returnValue(cold('--'));
        const checked1Spy = spyOnProperty(checkBox1, 'checked', 'set').and.callFake(() => { });
        const checked2Spy = spyOnProperty(checkBox2, 'checked', 'set').and.callFake(() => { });

        spyOnProperty(checkAllBox, 'stateChanged').and.returnValue(cold('-x', { x: TaskCheckAllStateEnum.AllChecked }));


        sut.registerCheckAllBox(checkAllBox);
        sut.registerTaskCheckBox(checkBox1);
        sut.registerTaskCheckBox(checkBox2);

        getTestScheduler().flush();

        expect(checked1Spy).toHaveBeenCalledTimes(1);
        expect(checked1Spy).toHaveBeenCalledWith(true);
        expect(checked2Spy).toHaveBeenCalledTimes(1);
        expect(checked2Spy).toHaveBeenCalledWith(true);

    });

    it('uncheck all should un check all registered check boxes', () => {
        spyOnProperty(checkBox1, 'checkedChange').and.returnValue(cold('--'));
        spyOnProperty(checkBox2, 'checkedChange').and.returnValue(cold('--'));
        const checked1Spy = spyOnProperty(checkBox1, 'checked', 'set').and.callFake(() => { });
        const checked2Spy = spyOnProperty(checkBox2, 'checked', 'set').and.callFake(() => { });
        spyOnProperty(checkAllBox, 'stateChanged').and.returnValue(cold('-x', { x: TaskCheckAllStateEnum.NoneChecked }));


        sut.registerCheckAllBox(checkAllBox);
        sut.registerTaskCheckBox(checkBox1);
        sut.registerTaskCheckBox(checkBox2);

        getTestScheduler().flush();

        expect(checked1Spy).toHaveBeenCalledTimes(1);
        expect(checked1Spy).toHaveBeenCalledWith(false);
        expect(checked2Spy).toHaveBeenCalledTimes(1);
        expect(checked2Spy).toHaveBeenCalledWith(false);
    });

    it('checking both registered check boxes to set the check all box to all checked', () => {
        spyOnProperty(checkBox1, 'checkedChange').and.returnValue(cold('-x', { x: true }));
        spyOnProperty(checkBox2, 'checkedChange').and.returnValue(cold('-x', { x: true }));
        spyOnProperty(checkBox1, 'checked', 'get').and.returnValue(true);
        spyOnProperty(checkBox2, 'checked', 'get').and.returnValue(true);

        spyOnProperty(checkAllBox, 'stateChanged').and.returnValue(cold('--'));
        let state: TaskCheckAllStateEnum;
        spyOnProperty(checkAllBox, 'state', 'set').and.callFake((value) => { state = value; });


        sut.registerCheckAllBox(checkAllBox);
        sut.registerTaskCheckBox(checkBox1);
        sut.registerTaskCheckBox(checkBox2);

        getTestScheduler().flush();

        expect(state).toEqual(TaskCheckAllStateEnum.AllChecked);
    });

    it('checking 1 registered check boxes to set the check all box to some checked', () => {
        spyOnProperty(checkBox1, 'checkedChange').and.returnValue(cold('-x', { x: true }));
        spyOnProperty(checkBox2, 'checkedChange').and.returnValue(cold('-x', { x: false }));
        spyOnProperty(checkBox1, 'checked', 'get').and.returnValue(true);
        spyOnProperty(checkBox2, 'checked', 'get').and.returnValue(false);

        spyOnProperty(checkAllBox, 'stateChanged').and.returnValue(cold('--'));
        let state: TaskCheckAllStateEnum;
        spyOnProperty(checkAllBox, 'state', 'set').and.callFake((value) => { state = value; });


        sut.registerCheckAllBox(checkAllBox);
        sut.registerTaskCheckBox(checkBox1);
        sut.registerTaskCheckBox(checkBox2);

        getTestScheduler().flush();

        expect(state).toEqual(TaskCheckAllStateEnum.SomeChecked);
    });

    it('checking 0 registered check boxes to set the check all box to none checked', () => {
        spyOnProperty(checkBox1, 'checkedChange').and.returnValue(cold('-x', { x: false }));
        spyOnProperty(checkBox2, 'checkedChange').and.returnValue(cold('-x', { x: false }));
        spyOnProperty(checkBox1, 'checked', 'get').and.returnValue(false);
        spyOnProperty(checkBox2, 'checked', 'get').and.returnValue(false);

        spyOnProperty(checkAllBox, 'stateChanged').and.returnValue(cold('--'));
        let state: TaskCheckAllStateEnum;
        spyOnProperty(checkAllBox, 'state', 'set').and.callFake((value) => { state = value; });


        sut.registerCheckAllBox(checkAllBox);
        sut.registerTaskCheckBox(checkBox1);
        sut.registerTaskCheckBox(checkBox2);

        getTestScheduler().flush();

        expect(state).toEqual(TaskCheckAllStateEnum.NoneChecked);
    });
});
