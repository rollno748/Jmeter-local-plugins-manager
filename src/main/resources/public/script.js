// This function is called when the form is submitted.
function submitForm() {
    const fields = {
        id: document.getElementById("id"),
        name: document.getElementById("name"),
        description: document.getElementById("description"),
        helpUrl: document.getElementById("helpUrl"),
        markerClass: document.getElementById("markerClass"),
        screenshotUrl: document.getElementById("screenshotUrl"),
        vendor: document.getElementById("vendor"),
        version: document.getElementById("version"),
        pluginJar: document.getElementById("pluginJar"),
        dependencyJars: document.getElementById("dependencyJars")
    }

    const requiredFields = ['id', 'name', 'description', 'helpUrl', 'markerClass', 'screenshotUrl', 'vendor', 'version'];
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

    var isFormFieldsEmpty;

    for (let field of requiredFields) {
        if (!fields[field].value) {
            isFormFieldsEmpty = true;
            break;
        }else{
            isFormFieldsEmpty = false;
        }
    }

    if (isFormFieldsEmpty === true){
        alert("All Fields are required");
    }else {
        var formData = new FormData();
        formData.append("id", id);
        formData.append("name", name);
        formData.append("description", description);
        formData.append("helpUrl", helpUrl);
        formData.append("markerClass", markerClass);
        formData.append("screenshotUrl", screenshotUrl);
        formData.append("vendor", vendor)
        formData.append("version", version);
        formData.append("pluginJar", pluginJar);

        for (var i = 0; i < dependencyJars.length; i++) {
            formData.append("dependencyJars", dependencyJars[i]);
        }

        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/v1/upload", true);
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    alert("Upload successful!");
                    fields.id.value = '';
                    fields.name.value = '';
                    fields.description.value = '';
                    fields.helpUrl.value = '';
                    fields.markerClass.value = '';
                    fields.screenshotUrl.value = '';
                    fields.vendor.value = '';
                    fields.version.value = '';
                    fields.pluginJar.value = '';
                    fields.dependencyJars.value = '';
                } else {
                    alert("Upload failed.");
                }
            }
        };
        xhr.send(formData);

//        // Redirect the user to the success page.
//        xhr.onload = function() {
//            if (xhr.status === 200) {
//                alert("Success");
//            } else {
//                alert("Something went wrong.");
//            }
//        };
    }
}
