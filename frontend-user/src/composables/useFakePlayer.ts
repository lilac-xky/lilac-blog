import { reactive, computed, onUnmounted, type InjectionKey } from 'vue';
import logo from '@/assets/logo.png';

export interface FakeTrack {
    title: string;
    artist: string;
    cover: string;
    duration: number;
    lyric: string;
    accent: string;
}

// === MOCK START === // TODO: 接入真实歌单后删除
const playlist: FakeTrack[] = [
    {
        title: '夜空中最亮的星',
        artist: '逃跑计划',
        cover: logo,
        duration: 252,
        lyric:
            '夜空中最亮的星  能否听清  那仰望的人  心底的孤独和叹息    Oh 夜空中最亮的星  能否记起  曾与我同行  消失在风里的身影    我祈祷拥有一颗透明的心灵  和会流泪的眼睛  给我再去相信的勇气  Oh 越过谎言去拥抱你    每当我找不到存在的意义  每当我迷失在黑夜里  夜空中最亮的星  请指引我靠近你',
        accent: 'linear-gradient(135deg, #38bdf8, #6366f1 60%, #ec4899)',
    },
    {
        title: '起风了',
        artist: '买辣椒也用券',
        cover: logo,
        duration: 326,
        lyric:
            '我曾将青春翻涌成她  也曾指尖弹出盛夏  心之所动  且就随缘去吧    逆着光行走  任风吹雨打    短短的路走走停停  也有了几分的距离  不知抚摸的是故事  还是段心情  也许期待的不过是与时间为敌    再次见到你  微凉晨光里  笑得很甜蜜  从前初识这世间  万般留恋  看着天边似在眼前',
        accent: 'linear-gradient(135deg, #fbbf24, #ec4899 50%, #8b5cf6)',
    },
    {
        title: '海阔天空',
        artist: 'Beyond',
        cover: logo,
        duration: 326,
        lyric:
            '今天我  寒夜里看雪飘过  怀着冷却了的心窝飘远方  风雨里追赶  雾里分不清影踪  天空海阔你与我  可会变 (谁没在变)    多少次  迎着冷眼与嘲笑  从没有放弃过心中的理想  一刹那恍惚  若有所失的感觉  不知不觉已变淡  心里爱 (谁明白我)    原谅我这一生不羁放纵爱自由  也会怕有一天会跌倒  Oh 背弃了理想  谁人都可以  哪会怕有一天只你共我',
        accent: 'linear-gradient(135deg, #5eead4, #38bdf8 55%, #818cf8)',
    },
];
// === MOCK END ===

export interface FakePlayerState {
    playing: boolean;
    currentTime: number;
    index: number;
    track: FakeTrack;
    duration: number;
    progress: number;
    play: () => void;
    pause: () => void;
    toggle: () => void;
    next: () => void;
    prev: () => void;
    seek: (ratio: number) => void;
}

export const FakePlayerKey: InjectionKey<FakePlayerState> = Symbol('fake-player');

export function useFakePlayer(): FakePlayerState {
    const state = reactive({
        playing: false,
        currentTime: 0,
        index: 0,
    });

    const track = computed(() => playlist[state.index]);
    const duration = computed(() => track.value.duration);
    const progress = computed(() =>
        duration.value > 0 ? state.currentTime / duration.value : 0
    );

    let timer: number | null = null;

    function startTick() {
        if (timer != null) return;
        timer = window.setInterval(() => {
            if (state.currentTime >= duration.value) {
                next();
                return;
            }
            state.currentTime += 1;
        }, 1000);
    }

    function stopTick() {
        if (timer != null) {
            window.clearInterval(timer);
            timer = null;
        }
    }

    function play() {
        state.playing = true;
        startTick();
    }

    function pause() {
        state.playing = false;
        stopTick();
    }

    function toggle() {
        state.playing ? pause() : play();
    }

    function next() {
        state.index = (state.index + 1) % playlist.length;
        state.currentTime = 0;
    }

    function prev() {
        state.index = (state.index - 1 + playlist.length) % playlist.length;
        state.currentTime = 0;
    }

    function seek(ratio: number) {
        const r = Math.max(0, Math.min(1, ratio));
        state.currentTime = Math.floor(duration.value * r);
    }

    onUnmounted(stopTick);

    return {
        get playing() {
            return state.playing;
        },
        get currentTime() {
            return state.currentTime;
        },
        get index() {
            return state.index;
        },
        get track() {
            return track.value;
        },
        get duration() {
            return duration.value;
        },
        get progress() {
            return progress.value;
        },
        play,
        pause,
        toggle,
        next,
        prev,
        seek,
    } as FakePlayerState;
}

export function formatTime(sec: number): string {
    const s = Math.max(0, Math.floor(sec));
    const m = Math.floor(s / 60);
    const ss = s % 60;
    return `${m.toString().padStart(2, '0')}:${ss.toString().padStart(2, '0')}`;
}
