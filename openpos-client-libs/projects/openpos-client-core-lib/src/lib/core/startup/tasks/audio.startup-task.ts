import { Injectable } from '@angular/core';
import { AudioInteractionService } from '../../audio/audio-interaction.service';
import { AudioRepositoryService } from '../../audio/audio-repository.service';
import { AudioService } from '../../audio/audio.service';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class AudioStartupTask implements StartupTask {
    name = 'AudioStartupTask';

    constructor(
        private audioRepositoryService: AudioRepositoryService,
        private audioService: AudioService,
        private audioInteractionService: AudioInteractionService
    ) { }

    async execute(): Promise<void> {
        console.debug('begin preloading audio configuration');
        await this.audioRepositoryService.loadConfig().toPromise();

        console.debug('begin preloading audio files');
        await this.audioRepositoryService.preloadAudio().toPromise();
        console.log('preloaded audio configuration and files');

        console.log('starting AudioService');
        this.audioService.listen();

        console.log('starting AudioInteractionService');
        this.audioInteractionService.listen();
    }
}
