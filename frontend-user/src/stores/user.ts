import { defineStore } from 'pinia';
import { ref } from 'vue';

const STORAGE_KEY = 'lilac-blog-user';

function readStorage(): API.LoginUserVO | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as API.LoginUserVO) : null;
  } catch {
    return null;
  }
}

export const useUserStore = defineStore('user', () => {
  const loginUser = ref<API.LoginUserVO | null>(readStorage());

  function setLoginUser(user: API.LoginUserVO) {
    loginUser.value = user;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
  }

  function clearLoginUser() {
    loginUser.value = null;
    localStorage.removeItem(STORAGE_KEY);
  }

  return { loginUser, setLoginUser, clearLoginUser };
});
