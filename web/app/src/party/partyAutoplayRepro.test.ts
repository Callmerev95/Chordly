// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/data/music/api', () => ({
  fetchRadio: vi.fn(async (videoId: string) => ({
    candidates: [`${videoId}r1`, `${videoId}r2`, `${videoId}r3`].map((id) => ({
      track: { videoId: id, title: `Radio ${id}`, artists: [{ name: 'X', id: null }], album: null, durationSec: 200, thumbnail: null },
      isVideo: false,
    })),
    seedIsVideo: false,
  })),
}));

import type { PartyTrack } from './models';
const { usePlayer } = await import('@/state/player');
const { useParty, party } = await import('./listenTogether');
const { EMPTY_PLAYBACK, EMPTY_QUEUE } = await import('./models');
const { startPartySync } = await import('./partySync');

let stop: (() => void) | null = null;

const track = (videoId: string, fromAutoplay = false): PartyTrack => ({
  videoId, title: videoId, artist: 'X', thumbnailUrl: null, durationMs: 240000, fromAutoplay,
});

/** Seeds the host's local player + party state for one track [seed]. */
function seedHostParty(seed: string) {
  const t = { videoId: seed, title: seed, artists: [{ name: 'X', id: null }], album: null, durationSec: 240, thumbnail: null, explicit: false, index: null, plays: null };
  usePlayer.setState({
    queue: [{ track: t, tier: 'user', source: { title: 'Debug', type: 'other' }, entryId: 'e0' }],
    index: 0,
    status: 'playing',
    autoplay: true,
    repeat: 'off',
  });
  useParty.setState({
    code: 'AB12CD',
    connection: 'live',
    clockSynced: true,
    hostOnlyControl: false,
    you: { memberId: 'H1', userId: 'u', displayName: 'Host', avatarUrl: null, isHost: true, connected: true, joinedAtMs: 0, lastSeenMs: 0 },
    members: [{ memberId: 'H1', userId: 'u', displayName: 'Host', avatarUrl: null, isHost: true, connected: true, joinedAtMs: 0, lastSeenMs: 0 }],
    playback: {
      seq: 1, queueSeq: 1, queueIndex: 0, queueLength: 1,
      track: track(seed),
      isPlaying: true, positionMs: 1000, anchorMs: Date.now(), effectivePositionMs: 1000,
      updatedBy: 'H1', startedBy: 'H1', startedByName: 'Host', autoplayEnabled: true, updatedAtMs: Date.now(),
    },
    queue: { seq: 1, index: 0, items: [track(seed)] },
  });
}

/** Server accepts queueAdd and echoes a queue frame asynchronously (like main.go). */
function mockServerEcho(): ReturnType<typeof vi.spyOn> {
  return vi.spyOn(party, 'queueAdd').mockImplementation((tracks) => {
    setTimeout(() => {
      const q = useParty.getState().queue;
      useParty.setState({ queue: { ...q, seq: q.seq + 1, items: [...q.items, ...tracks] } });
    }, 50);
  });
}

beforeEach(() => {
  vi.clearAllMocks();
  vi.useFakeTimers();
  usePlayer.setState({ queue: [], index: -1, status: 'idle', autoplay: true, repeat: 'off' });
  useParty.setState({
    code: null, you: null, members: [], playback: EMPTY_PLAYBACK, queue: EMPTY_QUEUE,
    connection: 'offline', clockSynced: false, hostOnlyControl: false,
  });
});

afterEach(() => {
  stop?.();
  stop = null;
  vi.useRealTimers();
  vi.restoreAllMocks();
});

describe('party autoplay', () => {
  it('tops up and syncs AutoPlay to the local queue', async () => {
    seedHostParty('A');
    const queueAddSpy = mockServerEcho();
    stop = startPartySync();

    await vi.advanceTimersByTimeAsync(6200);

    expect(queueAddSpy).toHaveBeenCalled();
    const localAutoplay = usePlayer.getState().queue.filter((e) => e.tier === 'autoplay');
    expect(localAutoplay.length).toBeGreaterThan(0);
  });

  it('carries on to the first AutoPlay track without wiping it from the server', async () => {
    seedHostParty('B');
    mockServerEcho();
    stop = startPartySync();

    await vi.advanceTimersByTimeAsync(6200);
    const first = usePlayer.getState().queue.find((e) => e.tier === 'autoplay');
    expect(first).toBeTruthy();
    const r1 = first!.track.videoId;

    // Track ends: the player moves itself onto the AutoPlay track; the party follows.
    usePlayer.setState({ index: usePlayer.getState().queue.findIndex((e) => e.tier === 'autoplay'), status: 'playing' });
    useParty.setState({
      playback: {
        ...useParty.getState().playback,
        track: track(r1, true),
        seq: useParty.getState().playback.seq + 1,
        queueIndex: usePlayer.getState().index,
      },
    });

    await vi.advanceTimersByTimeAsync(2000);

    const serverAutoplay = useParty.getState().queue.items.filter((t) => t.fromAutoplay).length;
    expect(serverAutoplay).toBeGreaterThan(0);
  });

  it('does not republish a stale local queue that would drop server AutoPlay', async () => {
    seedHostParty('A');
    // Server queues AutoPlay (r1), but this device's local queue hasn't synced it yet.
    useParty.setState((s) => ({ queue: { seq: s.queue.seq + 1, index: 0, items: [...s.queue.items, track('Ar1', true)] } }));

    // Capture onIntent so we can fire a publish like a finished track does.
    let hooks: { onIntent(): void } | null = null;
    const captured = vi.spyOn(await import('@/state/player'), 'setPartyHooks').mockImplementation((h) => {
      hooks = h as { onIntent(): void };
    });
    const setQueueSpy = vi.spyOn(party, 'setQueue');
    stop = startPartySync();
    void captured;

    expect(hooks).toBeTruthy();
    hooks!.onIntent();
    await vi.advanceTimersByTimeAsync(400);

    // A stale publish must not rewrite the server queue ([A] only, dropping r1).
    const dropped = setQueueSpy.mock.calls.some((args) => args[0]!.length === 1 && !args[0]![0]!.fromAutoplay);
    expect(dropped).toBe(false);
  });
});
