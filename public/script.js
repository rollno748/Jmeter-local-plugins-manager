// This function is called when the form is submitted.
function submitForm() {
  // Get the form data.
  var id = document.getElementById("id").value;
  var name = document.getElementById("name").value;
  var description = document.getElementById("description").value;
  var helpUrl = document.getElementById("helpUrl").value;
  var markerClass = document.getElementById("markerClass").value;
  var screenshotUrl = document.getElementById("screenshotUrl").value;
  var vendor = document.getElementById("vendor").value;
  var version = document.getElementById("version").value;
  var pluginJar = document.getElementById("pluginJar").files[0];
  var dependencyJars = document.getElementById("dependencyJars").files;

  // Send the form data to the server.
  var xhr = new XMLHttpRequest();
  xhr.open("POST", "/submit-form");
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.send(JSON.stringify({
    id: id,
    name: name,
    description: description,
    helpUrl: helpUrl,
    markerClass: markerClass,
    screenshotUrl: screenshotUrl,
    vendor: vendor,
    version: version,
    pluginJar: pluginJar,
    dependencyJars: dependencyJars
  }));

  // Redirect the user to the success page.
  xhr.onload = function() {
    if (xhr.status === 200) {
      window.location.href = "/success";
    } else {
      alert("Something went wrong.");
    }
  };
}

// Add an event listener to the submit button.
//document.getElementById("submit").addEventListener("click", submitForm);
