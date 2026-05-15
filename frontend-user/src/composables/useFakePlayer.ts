import { reactive, ref, computed, onUnmounted, type InjectionKey } from 'vue';

const OSS_BASE = 'https://lilacs.oss-cn-beijing.aliyuncs.com/lilac-blog';

export interface FakeTrack {
    title: string;
    artist: string;
    cover: string;
    url: string;
    lrcUrl: string;
    duration: number;
    lyric: string;
    accent: string;
}

export interface LyricLine {
    time: number;
    text: string;
}

const playlist: FakeTrack[] = [
    {
        title: '第57次取消发送',
        artist: '菲菲公主',
        cover: `${OSS_BASE}/cover/haikuotiankong.jpg`,
        url: `${OSS_BASE}/audio/%E8%8F%B2%E8%8F%B2%E5%85%AC%E4%B8%BB%EF%BC%88%E9%99%86%E7%BB%AE%E8%8F%B2%EF%BC%89%20-%20%E7%AC%AC57%E6%AC%A1%E5%8F%96%E6%B6%88%E5%8F%91%E9%80%81.mp3`,
        lrcUrl: `/audio/菲菲公主（陆绮菲） - 第57次取消发送.lrc`,
        duration: 326,
        lyric: '好像只能礼貌的问候 你的温柔也曾被我拥有',
        accent: 'linear-gradient(135deg, #5eead4, #38bdf8 55%, #818cf8)',
    },
    {
        title: '夜空中最亮的星',
        artist: '逃跑计划',
        cover: `${OSS_BASE}/cover/yekong.jpg`,
        url: `${OSS_BASE}/audio/%E9%80%83%E8%B7%91%E8%AE%A1%E5%88%92%20-%20%E5%A4%9C%E7%A9%BA%E4%B8%AD%E6%9C%80%E4%BA%AE%E7%9A%84%E6%98%9F.mp3`,
        lrcUrl: '/audio/逃跑计划 - 夜空中最亮的星.lrc',
        duration: 252,
        lyric:
            '夜空中最亮的星  能否听清  那仰望的人  心底的孤独和叹息    Oh 夜空中最亮的星  能否记起  曾与我同行  消失在风里的身影    我祈祷拥有一颗透明的心灵  和会流泪的眼睛  给我再去相信的勇气  Oh 越过谎言去拥抱你    每当我找不到存在的意义  每当我迷失在黑夜里  夜空中最亮的星  请指引我靠近你',
        accent: 'linear-gradient(135deg, #38bdf8, #6366f1 60%, #ec4899)',
    },
];

export interface FakePlayerState {
    playing: boolean;
    currentTime: number;
    index: number;
    track: FakeTrack;
    duration: number;
    progress: number;
    lyrics: LyricLine[];
    lyricIndex: number;
    currentLyric: LyricLine | null;
    play: () => void;
    pause: () => void;
    toggle: () => void;
    next: () => void;
    prev: () => void;
    seek: (ratio: number) => void;
}

export const FakePlayerKey: InjectionKey<FakePlayerState> = Symbol('fake-player');

const LRC_TIME_RE = /\[(\d{1,2}):(\d{1,2})(?:[.:](\d{1,3}))?\]/g;

function parseLrc(raw: string): LyricLine[] {
    const out: LyricLine[] = [];
    for (const rawLine of raw.split(/\r?\n/)) {
        if (!rawLine) continue;
        const stamps: number[] = [];
        let m: RegExpExecArray | null;
        LRC_TIME_RE.lastIndex = 0;
        while ((m = LRC_TIME_RE.exec(rawLine)) !== null) {
            const min = parseInt(m[1]!, 10);
            const sec = parseInt(m[2]!, 10);
            const fracRaw = m[3] ?? '0';
            const frac = parseInt(fracRaw.padEnd(3, '0').slice(0, 3), 10);
            stamps.push(min * 60 + sec + frac / 1000);
        }
        if (stamps.length === 0) continue;
        const text = rawLine.replace(LRC_TIME_RE, '').trim();
        if (!text) continue;
        for (const t of stamps) out.push({ time: t, text });
    }
    return out.sort((a, b) => a.time - b.time);
}

export function useFakePlayer(): FakePlayerState {
    const state = reactive({
        playing: false,
        currentTime: 0,
        index: 0,
        loadedDuration: 0,
    });

    const track = computed(() => playlist[state.index]!);
    const duration = computed(() =>
        state.loadedDuration > 0 ? state.loadedDuration : track.value.duration
    );
    const progress = computed(() =>
        duration.value > 0 ? state.currentTime / duration.value : 0
    );

    const lyrics = ref<LyricLine[]>([]);
    let lrcAbort: AbortController | null = null;

    async function loadLyrics(url: string) {
        if (lrcAbort) lrcAbort.abort();
        lrcAbort = new AbortController();
        lyrics.value = [];
        try {
            const res = await fetch(url, { signal: lrcAbort.signal });
            if (!res.ok) return;
            const text = await res.text();
            lyrics.value = parseLrc(text);
        } catch (e) {
            if ((e as Error).name !== 'AbortError') {
                console.warn('[player] lrc load failed:', url);
            }
        }
    }

    const lyricIndex = computed(() => {
        const arr = lyrics.value;
        if (arr.length === 0) return -1;
        const t = state.currentTime;
        let lo = 0;
        let hi = arr.length - 1;
        let ans = -1;
        while (lo <= hi) {
            const mid = (lo + hi) >> 1;
            if (arr[mid]!.time <= t) {
                ans = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return ans;
    });

    const currentLyric = computed<LyricLine | null>(() => {
        const i = lyricIndex.value;
        return i >= 0 ? lyrics.value[i]! : null;
    });

    const audio = new Audio();
    audio.preload = 'metadata';
    audio.src = track.value.url;
    loadLyrics(track.value.lrcUrl);

    function onTimeUpdate() {
        state.currentTime = audio.currentTime;
    }
    function onLoadedMetadata() {
        state.loadedDuration = Number.isFinite(audio.duration) ? audio.duration : 0;
    }
    function onEnded() {
        next();
    }
    function onPlay() {
        state.playing = true;
    }
    function onPause() {
        state.playing = false;
    }
    function onError() {
        console.warn('[player] audio load failed:', track.value.url);
        state.playing = false;
    }

    audio.addEventListener('timeupdate', onTimeUpdate);
    audio.addEventListener('loadedmetadata', onLoadedMetadata);
    audio.addEventListener('ended', onEnded);
    audio.addEventListener('play', onPlay);
    audio.addEventListener('pause', onPause);
    audio.addEventListener('error', onError);

    function loadCurrentTrack(autoPlay: boolean) {
        state.currentTime = 0;
        state.loadedDuration = 0;
        audio.src = track.value.url;
        audio.load();
        loadLyrics(track.value.lrcUrl);
        if (autoPlay) play();
    }

    function play() {
        audio.play().catch(() => {
            state.playing = false;
        });
    }

    function pause() {
        audio.pause();
    }

    function toggle() {
        audio.paused ? play() : pause();
    }

    function next() {
        const wasPlaying = state.playing;
        state.index = (state.index + 1) % playlist.length;
        loadCurrentTrack(wasPlaying);
    }

    function prev() {
        const wasPlaying = state.playing;
        state.index = (state.index - 1 + playlist.length) % playlist.length;
        loadCurrentTrack(wasPlaying);
    }

    function seek(ratio: number) {
        const r = Math.max(0, Math.min(1, ratio));
        const target = duration.value * r;
        if (Number.isFinite(target)) {
            audio.currentTime = target;
            state.currentTime = target;
        }
    }

    onUnmounted(() => {
        audio.removeEventListener('timeupdate', onTimeUpdate);
        audio.removeEventListener('loadedmetadata', onLoadedMetadata);
        audio.removeEventListener('ended', onEnded);
        audio.removeEventListener('play', onPlay);
        audio.removeEventListener('pause', onPause);
        audio.removeEventListener('error', onError);
        audio.pause();
        audio.src = '';
        if (lrcAbort) lrcAbort.abort();
    });

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
        get lyrics() {
            return lyrics.value;
        },
        get lyricIndex() {
            return lyricIndex.value;
        },
        get currentLyric() {
            return currentLyric.value;
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
