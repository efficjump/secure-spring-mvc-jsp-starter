(() => {
  "use strict";

  const root = document.getElementById("workspace");
  if (!root) {
    return;
  }

  const tabsContainer = document.getElementById("workspace-tabs");
  const panelsContainer = document.getElementById("workspace-panels");
  const emptyState = document.getElementById("workspace-empty");
  const activeViewTitle = document.getElementById("active-view-title");
  const toast = document.getElementById("workspace-toast");
  const menuSearch = document.getElementById("menu-search");
  const menuLinks = Array.from(root.querySelectorAll("[data-menu-key]"));
  const menuByKey = new Map(menuLinks.map((link) => [link.dataset.menuKey, link]));
  const openTabs = new Map();
  const maxTabs = Number.parseInt(root.dataset.maxTabs || "8", 10);
  const defaultMenuKey = root.dataset.defaultMenuKey;
  const stateKey = `${root.dataset.storageKey}:${root.dataset.user}`;
  let activeKey = null;
  let toastTimer = null;

  const readState = () => {
    try {
      const parsed = JSON.parse(window.sessionStorage.getItem(stateKey));
      if (!parsed || !Array.isArray(parsed.keys)) {
        return null;
      }
      return parsed;
    } catch (error) {
      return null;
    }
  };

  const saveState = () => {
    try {
      window.sessionStorage.setItem(stateKey, JSON.stringify({
        keys: Array.from(openTabs.keys()),
        activeKey,
        sidebarCollapsed: root.classList.contains("sidebar-collapsed")
      }));
    } catch (error) {
      // The workspace remains usable when browser storage is unavailable.
    }
  };

  const notify = (message) => {
    window.clearTimeout(toastTimer);
    toast.textContent = message;
    toast.hidden = false;
    toastTimer = window.setTimeout(() => {
      toast.hidden = true;
    }, 3200);
  };

  const embeddedUrl = (path) => {
    const url = new URL(path, window.location.origin);
    if (url.origin !== window.location.origin) {
      throw new Error("Only same-origin workspace views are allowed");
    }
    url.searchParams.set("embedded", "true");
    return url.toString();
  };

  const setLoading = (tabState, loading) => {
    tabState.wrapper.classList.toggle("is-loading", loading);
    tabState.frame.setAttribute("aria-busy", loading ? "true" : "false");
  };

  const handleFrameLoad = (tabState) => {
    setLoading(tabState, false);
    try {
      const frameLocation = new URL(tabState.frame.contentWindow.location.href);
      if (frameLocation.origin === window.location.origin && frameLocation.pathname.endsWith("/login")) {
        notify(root.dataset.sessionExpiredMessage);
        window.location.assign(frameLocation.href);
      }
    } catch (error) {
      // Security headers prevent cross-origin frames; ignore inaccessible transient documents.
    }
  };

  const createTab = (menuLink) => {
    const key = menuLink.dataset.menuKey;
    const label = menuLink.dataset.menuLabel;

    const wrapper = document.createElement("div");
    wrapper.className = "workspace-tab is-loading";
    wrapper.dataset.tabKey = key;

    const tab = document.createElement("button");
    tab.type = "button";
    tab.className = "workspace-tab-button";
    tab.id = `workspace-tab-${key}`;
    tab.setAttribute("role", "tab");
    tab.setAttribute("aria-selected", "false");
    tab.setAttribute("aria-controls", `workspace-panel-${key}`);
    tab.tabIndex = -1;
    tab.textContent = label;

    const close = document.createElement("button");
    close.type = "button";
    close.className = "workspace-tab-close";
    close.dataset.closeTab = key;
    close.setAttribute("aria-label", `${label} ${root.dataset.tabCloseLabel}`);
    close.textContent = "×";

    const loading = document.createElement("span");
    loading.className = "tab-loading";
    loading.setAttribute("aria-hidden", "true");

    wrapper.append(tab, close, loading);
    tabsContainer.append(wrapper);

    const panel = document.createElement("section");
    panel.className = "workspace-panel";
    panel.id = `workspace-panel-${key}`;
    panel.setAttribute("role", "tabpanel");
    panel.setAttribute("aria-labelledby", tab.id);
    panel.hidden = true;

    const frame = document.createElement("iframe");
    frame.className = "workspace-frame";
    frame.name = `workspace-frame-${key}`;
    frame.title = label;
    frame.src = embeddedUrl(menuLink.dataset.menuPath);
    frame.setAttribute("aria-busy", "true");
    panel.append(frame);
    panelsContainer.append(panel);

    const tabState = { key, label, wrapper, tab, panel, frame };
    tab.addEventListener("click", () => activateTab(key));
    tab.addEventListener("keydown", handleTabKeydown);
    close.addEventListener("click", () => closeTab(key));
    frame.addEventListener("load", () => handleFrameLoad(tabState));
    openTabs.set(key, tabState);
    return tabState;
  };

  const openTab = (key, activate = true) => {
    const menuLink = menuByKey.get(key);
    if (!menuLink) {
      return false;
    }
    if (!openTabs.has(key)) {
      if (openTabs.size >= maxTabs) {
        notify(root.dataset.tabLimitMessage);
        return false;
      }
      createTab(menuLink);
    }
    if (activate) {
      activateTab(key);
    }
    saveState();
    return true;
  };

  const activateTab = (key) => {
    const next = openTabs.get(key);
    if (!next) {
      return;
    }
    openTabs.forEach((tabState, tabKey) => {
      const selected = tabKey === key;
      tabState.wrapper.classList.toggle("is-active", selected);
      tabState.tab.setAttribute("aria-selected", selected ? "true" : "false");
      tabState.tab.tabIndex = selected ? 0 : -1;
      tabState.panel.hidden = !selected;
    });
    menuLinks.forEach((link) => {
      const selected = link.dataset.menuKey === key;
      link.classList.toggle("is-active", selected);
      if (selected) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });
    activeKey = key;
    activeViewTitle.textContent = next.label;
    emptyState.hidden = true;
    root.classList.remove("mobile-menu-open");
    saveState();
  };

  const closeTab = (key, ensureDefault = true) => {
    const tabState = openTabs.get(key);
    if (!tabState) {
      return;
    }
    const keys = Array.from(openTabs.keys());
    const closingIndex = keys.indexOf(key);
    const wasActive = activeKey === key;
    tabState.frame.src = "about:blank";
    tabState.wrapper.remove();
    tabState.panel.remove();
    openTabs.delete(key);

    if (wasActive) {
      const remaining = Array.from(openTabs.keys());
      const fallback = remaining[Math.min(closingIndex, remaining.length - 1)];
      activeKey = null;
      if (fallback) {
        activateTab(fallback);
      } else if (ensureDefault && defaultMenuKey && menuByKey.has(defaultMenuKey)) {
        openTab(defaultMenuKey);
      } else {
        activeViewTitle.textContent = "";
        emptyState.hidden = false;
      }
    }
    saveState();
  };

  const refreshActiveTab = () => {
    const tabState = openTabs.get(activeKey);
    if (!tabState) {
      return;
    }
    setLoading(tabState, true);
    try {
      tabState.frame.contentWindow.location.reload();
    } catch (error) {
      tabState.frame.src = embeddedUrl(menuByKey.get(activeKey).dataset.menuPath);
    }
  };

  const resetTabs = () => {
    Array.from(openTabs.keys()).forEach((key) => {
      if (key !== defaultMenuKey) {
        closeTab(key, false);
      }
    });
    if (defaultMenuKey && menuByKey.has(defaultMenuKey)) {
      openTab(defaultMenuKey);
    }
  };

  const handleTabKeydown = (event) => {
    if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) {
      return;
    }
    event.preventDefault();
    const keys = Array.from(openTabs.keys());
    const currentIndex = keys.indexOf(activeKey);
    let nextIndex = currentIndex;
    if (event.key === "ArrowLeft") {
      nextIndex = (currentIndex - 1 + keys.length) % keys.length;
    } else if (event.key === "ArrowRight") {
      nextIndex = (currentIndex + 1) % keys.length;
    } else if (event.key === "Home") {
      nextIndex = 0;
    } else if (event.key === "End") {
      nextIndex = keys.length - 1;
    }
    activateTab(keys[nextIndex]);
    openTabs.get(keys[nextIndex]).tab.focus();
  };

  const filterMenus = () => {
    const query = menuSearch.value.trim().toLocaleLowerCase();
    menuLinks.forEach((link) => {
      link.hidden = query.length > 0 && !link.dataset.menuLabel.toLocaleLowerCase().includes(query);
    });
    root.querySelectorAll("[data-menu-group]").forEach((group) => {
      group.hidden = !Array.from(group.querySelectorAll("[data-menu-key]")).some((link) => !link.hidden);
    });
  };

  menuLinks.forEach((link) => {
    link.addEventListener("click", (event) => {
      event.preventDefault();
      openTab(link.dataset.menuKey);
    });
  });

  root.querySelectorAll("[data-action]").forEach((button) => {
    button.addEventListener("click", () => {
      switch (button.dataset.action) {
        case "toggle-sidebar":
          root.classList.toggle("sidebar-collapsed");
          saveState();
          break;
        case "toggle-mobile-menu":
          root.classList.toggle("mobile-menu-open");
          break;
        case "close-mobile-menu":
          root.classList.remove("mobile-menu-open");
          break;
        case "refresh-tab":
          refreshActiveTab();
          break;
        case "close-other-tabs":
          resetTabs();
          break;
        default:
          break;
      }
    });
  });

  menuSearch.addEventListener("input", filterMenus);

  const savedState = readState();
  if (savedState?.sidebarCollapsed) {
    root.classList.add("sidebar-collapsed");
  }
  const restorableKeys = savedState?.keys.filter((key) => menuByKey.has(key)).slice(0, maxTabs) || [];
  restorableKeys.forEach((key) => openTab(key, false));
  const initialKey = savedState?.activeKey && openTabs.has(savedState.activeKey)
    ? savedState.activeKey
    : (openTabs.has(defaultMenuKey) ? defaultMenuKey : restorableKeys[0]);

  if (initialKey) {
    activateTab(initialKey);
  } else if (defaultMenuKey && menuByKey.has(defaultMenuKey)) {
    openTab(defaultMenuKey);
  } else if (menuLinks.length > 0) {
    openTab(menuLinks[0].dataset.menuKey);
  } else {
    activeViewTitle.textContent = "";
    emptyState.hidden = false;
  }
})();
