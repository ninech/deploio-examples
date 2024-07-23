#[macro_use]
extern crate rocket;

use rocket::response::content::RawHtml;

#[get("/")]
fn index() -> RawHtml<String> {
    let bytes = include_bytes!("../static/index.html");
    let index = String::from_utf8_lossy(bytes);
    return RawHtml(index.to_string());
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![index])
}
