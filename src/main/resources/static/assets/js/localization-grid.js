(() => {
  "use strict";

  const grids = Array.from(document.querySelectorAll("[data-editable-grid]"));
  if (grids.length === 0) {
    return;
  }

  let submitting = false;

  function controlValue(control) {
    if (control instanceof HTMLInputElement && control.type === "checkbox") {
      return String(control.checked);
    }
    return control.value;
  }

  function rowControls(row) {
    return Array.from(row.querySelectorAll("[data-original-value]"));
  }

  function refreshRow(row) {
    const controls = rowControls(row);
    let dirty = false;
    controls.forEach((control) => {
      const changed = controlValue(control) !== control.dataset.originalValue;
      control.classList.toggle("is-dirty", changed);
      dirty ||= changed;
    });
    row.classList.toggle("is-dirty", dirty);
    const indicator = row.querySelector("[data-dirty-indicator]");
    if (indicator) {
      indicator.hidden = !dirty;
    }
  }

  function refreshGrid(grid) {
    const dirtyRows = grid.querySelectorAll("[data-editable-row].is-dirty").length;
    const summary = grid.querySelector("[data-dirty-summary]");
    if (summary) {
      const label = grid.dataset.dirtyLabel || "{0}";
      summary.textContent = dirtyRows > 0 ? label.replace("{0}", String(dirtyRows)) : "";
      summary.hidden = dirtyRows === 0;
    }
  }

  function refresh(grid, row) {
    refreshRow(row);
    refreshGrid(grid);
  }

  grids.forEach((grid) => {
    grid.querySelectorAll("[data-original-value]").forEach((control) => {
      control.dataset.originalValue = controlValue(control);
    });
    grid.querySelectorAll("[data-editable-row]").forEach((row) => refreshRow(row));
    refreshGrid(grid);

    grid.addEventListener("input", (event) => {
      const control = event.target.closest("[data-original-value]");
      const row = control?.closest("[data-editable-row]");
      if (control && row) {
        refresh(grid, row);
      }
    });

    grid.addEventListener("change", (event) => {
      const control = event.target.closest("[data-original-value]");
      const row = control?.closest("[data-editable-row]");
      if (control && row) {
        refresh(grid, row);
      }
    });

    grid.addEventListener("keydown", (event) => {
      if (!(event.target instanceof HTMLTextAreaElement)
          || !(event.metaKey || event.ctrlKey)
          || event.key !== "Enter") {
        return;
      }
      const formId = event.target.getAttribute("form");
      const form = formId ? document.getElementById(formId) : null;
      if (form instanceof HTMLFormElement) {
        event.preventDefault();
        form.requestSubmit();
      }
    });
  });

  document.addEventListener("submit", () => {
    submitting = true;
  });

  window.addEventListener("beforeunload", (event) => {
    if (!submitting && document.querySelector("[data-editable-row].is-dirty")) {
      event.preventDefault();
      event.returnValue = "";
    }
  });
})();
